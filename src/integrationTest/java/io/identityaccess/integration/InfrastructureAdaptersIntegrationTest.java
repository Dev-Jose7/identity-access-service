package io.identityaccess.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.identityaccess.application.command.RegisterCommand;
import io.identityaccess.application.port.in.RegisterCommandUseCase;
import io.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import io.identityaccess.application.port.out.persistence.AccessCatalogPersistencePort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.service.OutboxEventRelayPublisher;
import io.identityaccess.domain.event.DomainEvent;
import io.identityaccess.domain.exception.ExclusiveRoleAlreadyAssignedException;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.exception.PrimaryAccountAlreadyExistsException;
import io.identityaccess.domain.exception.RateLimitExceededException;
import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.session.enumtype.SessionStatus;
import io.identityaccess.domain.model.session.valueobject.AccessJti;
import io.identityaccess.domain.model.session.valueobject.ClientDevice;
import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.model.session.valueobject.RefreshJti;
import io.identityaccess.domain.model.session.valueobject.SessionId;
import io.identityaccess.domain.model.session.valueobject.SessionTimestamps;
import io.identityaccess.domain.model.user.UserAggregate;
import io.identityaccess.domain.model.user.RegistrationMode;
import io.identityaccess.domain.model.user.entity.UserLoginAttempt;
import io.identityaccess.domain.model.user.event.AccountRegisteredEvent;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import io.identityaccess.domain.model.user.valueobject.UserId;
import io.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveOutboxEventRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Testcontainers
class InfrastructureAdaptersIntegrationTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:15");
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7");
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("confluentinc/cp-kafka:7.6.1");

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(POSTGRES_IMAGE)
            .withDatabaseName("identity_access_it")
            .withUsername("identity_access_it")
            .withPassword("identity_access_it");

    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(6379);

    @Container
    static final ConfluentKafkaContainer KAFKA = new ConfluentKafkaContainer(KAFKA_IMAGE);

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getMappedPort(5432) + "/" + POSTGRES.getDatabaseName());
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.producer.acks", () -> "all");
        registry.add("app.database.schema.initialize-on-startup", () -> "true");
        registry.add("app.outbox.relay.enabled", () -> "false");
        registry.add("app.sessions.expiration.enabled", () -> "false");
        registry.add("app.redis.rate-limit.login-requests-per-minute", () -> "2");
        registry.add("app.redis.rate-limit.refresh-requests-per-minute", () -> "2");
        registry.add("app.redis.rate-limit.registration-requests-per-minute", () -> "2");
    }

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private AccessCatalogPersistencePort accessCatalogPersistencePort;

    @Autowired
    private UserPersistencePort userPersistencePort;

    @Autowired
    private RegisterCommandUseCase registerCommandUseCase;

    @Autowired
    private SessionPersistencePort sessionPersistencePort;

    @Autowired
    private OutboxPersistencePort outboxPersistencePort;

    @Autowired
    private OutboxEventRelayPublisher outboxEventRelayPublisher;

    @Autowired
    private ReactiveOutboxEventRepository reactiveOutboxEventRepository;

    @Autowired
    private SecurityRateLimitPort securityRateLimitPort;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Test
    void schemaAndSeedShouldCreateAuthorizationCatalog() {
        Mono<Long> roleCount = count("role");
        Mono<Long> permissionCount = count("permission");
        Mono<Long> grantCount = count("role_permission");
        Mono<Long> assignmentPolicyCount = count("role_assignment_policy");

        StepVerifier.create(Mono.zip(roleCount, permissionCount, grantCount, assignmentPolicyCount))
                .assertNext(tuple -> {
                    assertTrue(tuple.getT1() >= 5, "seeded roles are required");
                    assertTrue(tuple.getT2() >= 16, "seeded permissions are required");
                    assertTrue(tuple.getT3() >= 1, "seeded role permissions are required");
                    assertTrue(tuple.getT4() >= 1, "seeded assignment policy is required");
                })
                .verifyComplete();
    }

    @Test
    void accessCatalogAdapterShouldCreatePermissionAndGrantItToRole() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String roleCode = "ACCESS_IT_" + suffix.toUpperCase();
        String permissionCode = "iam.it." + suffix;

        StepVerifier.create(accessCatalogPersistencePort.createRole(roleCode, "Integration role", false, now)
                        .flatMap(role -> accessCatalogPersistencePort
                                .createPermission(permissionCode, "iam.it", "read", "GLOBAL", "Integration permission", false, now)
                                .flatMap(permission -> accessCatalogPersistencePort.grantPermissionToRole(role.roleId(), permission.permissionCode(), now))
                                .flatMap(grant -> permissionGrantExists(grant.roleId(), grant.permissionCode()))))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void userAndSessionAdaptersShouldPersistAuthorizationAndRevokeActiveSession() {
        String email = "it-" + UUID.randomUUID() + "@example.test";
        UserAggregate user = UserAggregate.register(EmailAddress.of(email), "hashed-password", java.util.Set.of("ACCOUNT_USER"));
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        SessionAggregate session = SessionAggregate.rehydrate(
                SessionId.newId(),
                user.id(),
                ClientDevice.of("device-it", "Integration Test", "test"),
                ClientIp.of("203.0.113.10"),
                AccessJti.newId(),
                RefreshJti.newId(),
                SessionTimestamps.of(now, now.plusSeconds(900), now.plusSeconds(604800)),
                SessionStatus.ACTIVE,
                null);

        StepVerifier.create(userPersistencePort.create(user, "ACCOUNT_USER", "SYSTEM_TEST")
                        .then(userPersistencePort.loadAuthorizationSnapshot(user.id()))
                        .flatMap(snapshot -> {
                            assertTrue(snapshot.roles().contains("ACCOUNT_USER"));
                            assertTrue(snapshot.permissions().contains("iam.permission.read"));
                            return userPersistencePort.existsByRoleCode("ACCOUNT_USER");
                        })
                        .flatMap(exists -> {
                            assertTrue(exists, "created account must be discoverable by assigned role");
                            return sessionPersistencePort.create(session);
                        })
                        .then(sessionPersistencePort.findActiveByRefreshJti(session.refreshJti()))
                        .flatMap(found -> sessionPersistencePort.revokeActiveSessionsByUserId(user.id(), "IT_REVOKE", now)
                                .thenReturn(found)))
                .assertNext(found -> assertEquals(session.id().value(), found.id().value()))
                .verifyComplete();

        StepVerifier.create(sessionPersistencePort.findActiveByRefreshJti(session.refreshJti()))
                .expectError()
                .verify();
    }

    @Test
    void userAndSessionAdaptersShouldUnblockAccountAndExpireRefreshExpiredSession() {
        String email = "unblock-expire-it-" + UUID.randomUUID() + "@example.test";
        UserAggregate user = UserAggregate.register(EmailAddress.of(email), "hashed-password", Set.of("ACCOUNT_USER"));
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Instant sweepAt = Instant.parse("2026-01-02T00:00:00Z");
        SessionAggregate session = SessionAggregate.rehydrate(
                SessionId.newId(),
                user.id(),
                ClientDevice.of("device-expire-it", "Integration Test", "test"),
                ClientIp.of("203.0.113.12"),
                AccessJti.newId(),
                RefreshJti.newId(),
                SessionTimestamps.of(createdAt, createdAt.plusSeconds(900), createdAt.plusSeconds(3600)),
                SessionStatus.ACTIVE,
                null);

        StepVerifier.create(userPersistencePort.create(user, "ACCOUNT_USER", "SYSTEM_TEST")
                        .then(userPersistencePort.block(user.id(), sweepAt))
                        .then(userPersistencePort.unblock(user.id(), sweepAt.plusSeconds(1)))
                        .then(sessionPersistencePort.create(session))
                        .then(sessionPersistencePort.expireExpiredSessions(sweepAt))
                        .flatMap(expiredSessions -> Mono.zip(
                                userPersistencePort.loadStatus(user.id()),
                                sessionPersistencePort.listSessions()
                                        .filter(foundSession -> session.id().value().equals(foundSession.sessionId()))
                                        .single(),
                                Mono.just(expiredSessions))))
                .assertNext(tuple -> {
                    assertEquals("ACTIVE", tuple.getT1().status());
                    assertEquals("EXPIRED", tuple.getT2().status());
                    assertEquals(1L, tuple.getT3());
                })
                .verifyComplete();

        StepVerifier.create(sessionPersistencePort.findActiveByRefreshJti(session.refreshJti()))
                .expectError()
                .verify();
    }

    @Test
    void userAdapterShouldPersistFailedLoginAttemptAndIncrementAccountCounter() {
        String email = "failed-login-it-" + UUID.randomUUID() + "@example.test";
        UserAggregate user = UserAggregate.register(EmailAddress.of(email), "hashed-password", Set.of("ACCOUNT_USER"));
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        StepVerifier.create(userPersistencePort.create(user, "ACCOUNT_USER", "SYSTEM_TEST")
                        .then(userPersistencePort.recordLoginAttempt(UserLoginAttempt.failed(
                                user.id(),
                                ClientIp.of("203.0.113.14"),
                                now)))
                        .then(userPersistencePort.listAccounts()
                                .filter(account -> user.id().value().equals(account.userId()))
                                .single()))
                .assertNext(account -> assertEquals(1, account.failedLoginCount()))
                .verifyComplete();
    }

    @Test
    void readAdaptersShouldListAccountsSessionsAndRolePermissions() {
        String email = "read-it-" + UUID.randomUUID() + "@example.test";
        UserAggregate user = UserAggregate.register(EmailAddress.of(email), "hashed-password", Set.of("ACCOUNT_USER"));
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        SessionAggregate session = SessionAggregate.rehydrate(
                SessionId.newId(),
                user.id(),
                ClientDevice.of("device-read-it", "Integration Test", "test"),
                ClientIp.of("203.0.113.11"),
                AccessJti.newId(),
                RefreshJti.newId(),
                SessionTimestamps.of(now, now.plusSeconds(900), now.plusSeconds(604800)),
                SessionStatus.ACTIVE,
                null);

        StepVerifier.create(userPersistencePort.create(user, "ACCOUNT_USER", "SYSTEM_TEST")
                        .then(sessionPersistencePort.create(session))
                        .then(Mono.zip(
                                userPersistencePort.listAccounts()
                                        .filter(account -> user.id().value().equals(account.userId()))
                                        .single(),
                                sessionPersistencePort.listSessions()
                                        .filter(foundSession -> session.id().value().equals(foundSession.sessionId()))
                                        .single(),
                                roleIdByCode("ACCOUNT_USER")
                                        .flatMapMany(accessCatalogPersistencePort::listPermissionsByRoleId)
                                        .collectList())))
                .assertNext(tuple -> {
                    assertEquals(email, tuple.getT1().email());
                    assertTrue(tuple.getT1().roles().contains("ACCOUNT_USER"));
                    assertEquals(session.id().value(), tuple.getT2().sessionId());
                    assertEquals("ACTIVE", tuple.getT2().status());
                    assertTrue(tuple.getT3().stream()
                            .anyMatch(permission -> "iam.permission.read".equals(permission.permissionCode())));
                })
                .verifyComplete();
    }

    @Test
    void registrationUseCaseShouldCreatePrimaryAndPublicAccountsWithDifferentInitialRoles() {
        String suffix = UUID.randomUUID().toString();
        RegisterCommand publicCommand = new RegisterCommand(
                "public-" + suffix + "@example.test",
                "User123!",
                RegistrationMode.PUBLIC_SELF_REGISTRATION,
                null,
                null,
                "Integration Test",
                "198.51.100.21");

        StepVerifier.create(ensureSystemAdminExists()
                        .flatMap(primaryUserId -> userPersistencePort.loadAuthorizationSnapshot(UserId.of(primaryUserId)))
                        .zipWith(registerCommandUseCase
                                .handle(publicCommand)
                                .flatMap(publicAccount -> userPersistencePort.loadAuthorizationSnapshot(UserId.of(publicAccount.userId())))))
                .assertNext(tuple -> {
                    assertTrue(tuple.getT1().roles().contains("SYSTEM_ADMIN"));
                    assertTrue(tuple.getT2().roles().contains("ACCOUNT_USER"));
                    assertTrue(!tuple.getT2().roles().contains("SYSTEM_ADMIN"));
                })
                .verifyComplete();
    }

    @Test
    void registrationUseCaseShouldRejectSecondPrimaryAccount() {
        String suffix = UUID.randomUUID().toString();
        RegisterCommand secondPrimary = new RegisterCommand(
                "primary-two-" + suffix + "@example.test",
                "Admin123!",
                RegistrationMode.PRIMARY_REGISTRATION,
                null,
                null,
                "Integration Test",
                "198.51.100.31");

        StepVerifier.create(ensureSystemAdminExists().then(registerCommandUseCase.handle(secondPrimary)))
                .expectError(PrimaryAccountAlreadyExistsException.class)
                .verify();
    }

    @Test
    void databaseShouldRejectConcurrentBypassOfExclusiveRoleAssignment() {
        String email = "exclusive-bypass-" + UUID.randomUUID() + "@example.test";
        UserAggregate user = UserAggregate.register(EmailAddress.of(email), "hashed-password", java.util.Set.of("SYSTEM_ADMIN"));

        StepVerifier.create(ensureSystemAdminExists().then(userPersistencePort.create(user, "SYSTEM_ADMIN", "SYSTEM_TEST")))
                .expectError(ExclusiveRoleAlreadyAssignedException.class)
                .verify();
    }

    @Test
    void shouldRejectBlockingLastActiveExclusiveRoleHolder() {
        StepVerifier.create(ensureSystemAdminExists()
                        .flatMap(userId -> userPersistencePort.wouldBlockLastActiveExclusiveRoleHolder(UserId.of(userId))))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void accessCatalogShouldRejectMutationOfProtectedSystemAdminRole() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");

        StepVerifier.create(roleIdByCode("SYSTEM_ADMIN")
                        .flatMap(roleId -> accessCatalogPersistencePort.disableRole(roleId, now)))
                .expectError(OperationNotPermittedException.class)
                .verify();
    }

    @Test
    void outboxRelayShouldPublishPendingEventToKafkaAndMarkItAsPublished() {
        String accountId = "acc-it-" + UUID.randomUUID();
        var event = AccountRegisteredEvent.create(
                UserId.of(accountId),
                EmailAddress.of("outbox-" + UUID.randomUUID() + "@example.test"),
                Instant.parse("2026-01-01T00:00:00Z"));

        StepVerifier.create(outboxPersistencePort.store(event)
                        .then(outboxEventRelayPublisher.publishPending(10, 3))
                        .then(reactiveOutboxEventRepository.findById(event.eventId())))
                .assertNext(row -> assertEquals("PUBLISHED", row.status()))
                .verifyComplete();

        ConsumerRecord<String, String> record = consumeOne("iam.account-registered.v1", accountId);
        assertEquals(accountId, record.key());
        assertTrue(record.value().contains(accountId));
    }

    @Test
    void outboxRelayShouldPersistFailureMetadataWhenPublicationFails() {
        DomainEvent event = unmappedEvent();

        StepVerifier.create(outboxPersistencePort.store(event)
                        .then(outboxEventRelayPublisher.publishPending(10, 1))
                        .then(reactiveOutboxEventRepository.findById(event.eventId())))
                .assertNext(row -> {
                    assertEquals("FAILED", row.status());
                    assertEquals(1, row.retryCount());
                    assertTrue(row.lastError().contains("No Kafka topic mapping"));
                })
                .verifyComplete();
    }

    @Test
    void redisRateLimiterShouldIncrementSetTtlAndRejectWhenThresholdIsExceeded() {
        EmailAddress email = EmailAddress.of("rate-" + UUID.randomUUID() + "@example.test");
        ClientIp clientIp = ClientIp.of("198.51.100.10");
        String key = "iam:rate:login:" + email.normalized() + ":" + clientIp.value();

        StepVerifier.create(securityRateLimitPort.ensureLoginAllowed(email, clientIp)
                        .then(securityRateLimitPort.ensureLoginAllowed(email, clientIp))
                        .then(redisTemplate.getExpire(key)))
                .assertNext(ttl -> assertTrue(ttl.getSeconds() > 0, "rate limit key must have ttl"))
                .verifyComplete();

        StepVerifier.create(securityRateLimitPort.ensureLoginAllowed(email, clientIp))
                .expectError(RateLimitExceededException.class)
                .verify();
    }

    private Mono<Long> count(String tableName) {
        return databaseClient.sql("SELECT COUNT(*) AS total FROM " + tableName)
                .map((row, metadata) -> row.get("total", Long.class))
                .one();
    }

    private Mono<Boolean> permissionGrantExists(String roleId, String permissionCode) {
        return databaseClient.sql("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM role_permission
                            WHERE role_id = :roleId
                              AND permission_code = :permissionCode
                        ) AS exists_value
                        """)
                .bind("roleId", roleId)
                .bind("permissionCode", permissionCode)
                .map((row, metadata) -> Boolean.TRUE.equals(row.get("exists_value", Boolean.class)))
                .one();
    }

    private Mono<String> roleIdByCode(String roleCode) {
        return databaseClient.sql("""
                        SELECT role_id
                        FROM role
                        WHERE role_code = :roleCode
                        """)
                .bind("roleCode", roleCode)
                .map((row, metadata) -> row.get("role_id", String.class))
                .one();
    }

    private Mono<String> ensureSystemAdminExists() {
        return activeUserIdByRoleCode("SYSTEM_ADMIN")
                .switchIfEmpty(registerCommandUseCase.handle(new RegisterCommand(
                                "primary-" + UUID.randomUUID() + "@example.test",
                                "Admin123!",
                                RegistrationMode.PRIMARY_REGISTRATION,
                                null,
                                null,
                                "Integration Test",
                                "198.51.100.29"))
                        .map(result -> result.userId()));
    }

    private Mono<String> activeUserIdByRoleCode(String roleCode) {
        return databaseClient.sql("""
                        SELECT ura.user_id
                        FROM user_role_assignment ura
                        JOIN role r ON r.role_id = ura.role_id
                        JOIN user_account ua ON ua.user_id = ura.user_id
                        WHERE r.role_code = :roleCode
                          AND r.status = 'ACTIVE'
                          AND ura.status = 'ACTIVE'
                          AND ua.status = 'ACTIVE'
                        LIMIT 1
                        """)
                .bind("roleCode", roleCode)
                .map((row, metadata) -> row.get("user_id", String.class))
                .one();
    }

    private ConsumerRecord<String, String> consumeOne(String topic, String expectedKey) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "identity-access-it-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(List.of(topic));
            long deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
            while (System.nanoTime() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> record : records) {
                    if (expectedKey.equals(record.key())) {
                        return record;
                    }
                }
            }
        }
        return fail("No Kafka record consumed from topic " + topic + " with key " + expectedKey);
    }

    private DomainEvent unmappedEvent() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        return new DomainEvent() {
            private final String eventId = "evt-unmapped-" + UUID.randomUUID();

            @Override
            public String eventId() {
                return eventId;
            }

            @Override
            public String eventType() {
                return "UnmappedIntegrationEvent";
            }

            @Override
            public Instant occurredAt() {
                return now;
            }

            @Override
            public String aggregateId() {
                return "aggregate-unmapped";
            }

            @Override
            public Map<String, Object> payload() {
                return Map.of("eventId", eventId);
            }
        };
    }
}
