package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.identityaccess.application.command.catalog.CreateRoleCommand;
import io.identityaccess.application.command.catalog.GrantPermissionToRoleCommand;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.AccessCatalogPersistencePort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.result.catalog.PermissionGrantResult;
import io.identityaccess.application.result.catalog.RoleCatalogResult;
import io.identityaccess.application.usecase.command.AccessCatalogUseCase;
import io.identityaccess.domain.event.DomainEvent;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AccessCatalogUseCaseTest {

    @Mock
    private AccessCatalogPersistencePort accessCatalogPersistencePort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldCreateRoleAndEmitAuditAndOutboxEvent() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AccessCatalogUseCase useCase = useCase();
        when(clockPort.now()).thenReturn(now);
        when(accessCatalogPersistencePort.createRole("ACCESS_OPERATOR", "Operator", false, now))
                .thenReturn(Mono.just(new AccessCatalogPersistencePort.RoleRecord(
                        "role-1", "ACCESS_OPERATOR", "Operator", "ACTIVE", false)));
        when(securityAuditPort.recordAccessCatalogChanged(
                "actor-1", "ROLE_CREATED", "ROLE", "role-1", "ACCESS_OPERATOR", true))
                .thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any(DomainEvent.class))).thenReturn(Mono.just(outboxRow("evt-1", "RoleCreated")));

        RoleCatalogResult result = useCase.createRole(new CreateRoleCommand(
                        "ACCESS_OPERATOR", "Operator", false, "actor-1"))
                .block();

        assertNotNull(result);
        assertEquals("role-1", result.roleId());
        assertEquals("ACCESS_OPERATOR", result.roleCode());
        assertEquals("ACTIVE", result.status());
        verify(outboxPersistencePort).store(any(DomainEvent.class));
    }

    @Test
    void shouldNotEmitOutboxEventWhenPermissionWasAlreadyGrantedToRole() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AccessCatalogUseCase useCase = useCase();
        when(clockPort.now()).thenReturn(now);
        when(accessCatalogPersistencePort.grantPermissionToRole("role-1", "iam.account.read", now))
                .thenReturn(Mono.just(new AccessCatalogPersistencePort.PermissionGrantRecord(
                        "role-1", "iam.account.read", false)));
        when(securityAuditPort.recordAccessCatalogChanged(
                "actor-1", "PERMISSION_GRANTED_TO_ROLE", "ROLE", "role-1", "iam.account.read", false))
                .thenReturn(Mono.empty());

        PermissionGrantResult result = useCase.grantPermissionToRole(new GrantPermissionToRoleCommand(
                        "role-1", "iam.account.read", "actor-1"))
                .block();

        assertNotNull(result);
        assertFalse(result.changed());
        assertEquals("ALREADY_GRANTED", result.status());
        verify(outboxPersistencePort, never()).store(any(DomainEvent.class));
    }

    @Test
    void shouldListPermissionsByRoleId() {
        AccessCatalogUseCase useCase = useCase();
        when(accessCatalogPersistencePort.listPermissionsByRoleId("role-1"))
                .thenReturn(Flux.just(new AccessCatalogPersistencePort.PermissionRecord(
                        "perm-1",
                        "iam.account.read",
                        "iam.account",
                        "read",
                        "GLOBAL",
                        "Read accounts",
                        "ACTIVE",
                        true)));

        StepVerifier.create(useCase.listPermissionsByRoleId("role-1"))
                .assertNext(result -> {
                    assertEquals("perm-1", result.permissionId());
                    assertEquals("iam.account.read", result.permissionCode());
                    assertEquals("iam.account", result.resource());
                    assertEquals("read", result.action());
                })
                .verifyComplete();
    }

    @Test
    void shouldEmitOutboxEventWhenPermissionIsGrantedToRole() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AccessCatalogUseCase useCase = useCase();
        when(clockPort.now()).thenReturn(now);
        when(accessCatalogPersistencePort.grantPermissionToRole("role-1", "iam.account.read", now))
                .thenReturn(Mono.just(new AccessCatalogPersistencePort.PermissionGrantRecord(
                        "role-1", "iam.account.read", true)));
        when(securityAuditPort.recordAccessCatalogChanged(
                "actor-1", "PERMISSION_GRANTED_TO_ROLE", "ROLE", "role-1", "iam.account.read", true))
                .thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any(DomainEvent.class))).thenReturn(Mono.just(outboxRow("evt-2", "PermissionGrantedToRole")));

        PermissionGrantResult result = useCase.grantPermissionToRole(new GrantPermissionToRoleCommand(
                        "role-1", "iam.account.read", "actor-1"))
                .block();

        assertNotNull(result);
        assertTrue(result.changed());
        assertEquals("GRANTED", result.status());
        verify(outboxPersistencePort).store(any(DomainEvent.class));
    }

    private AccessCatalogUseCase useCase() {
        return new AccessCatalogUseCase(
                accessCatalogPersistencePort,
                outboxPersistencePort,
                securityAuditPort,
                clockPort);
    }

    private OutboxEventRow outboxRow(String eventId, String eventType) {
        return new OutboxEventRow(
                eventId,
                "Access",
                "aggregate-1",
                eventType,
                "{}",
                "PENDING",
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                0,
                null,
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }
}
