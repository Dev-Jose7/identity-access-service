package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.command.RegisterCommand;
import com.arka.identityaccess.application.mapper.command.RegisterCommandAssembler;
import com.arka.identityaccess.application.mapper.result.RegisterResultMapper;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.RoleAssignmentPolicyPort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.port.out.security.PasswordHashPort;
import com.arka.identityaccess.application.result.RegisterResult;
import com.arka.identityaccess.application.usecase.command.RegisterUseCase;
import com.arka.identityaccess.domain.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.exception.RateLimitExceededException;
import com.arka.identityaccess.domain.model.user.RegistrationMode;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.service.UserRegistrationPolicy;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    @Mock
    private SecurityRateLimitPort securityRateLimitPort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordHashPort passwordHashPort;

    @Mock
    private RoleAssignmentPolicyPort roleAssignmentPolicyPort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Test
    void shouldCreateFounderInPublicRegisterFounderFlow() {
        RegisterUseCase useCase = new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new UserRegistrationPolicy(),
                roleAssignmentPolicyPort,
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                new RegisterResultMapper());

        RegisterCommand command = new RegisterCommand(
                "founder@arka.com",
                "PlainSecret123",
                RegistrationMode.ONBOARDING_OWNER,
                null,
                null,
                "JUnit",
                "127.0.0.1");

        UserAggregate created = UserAggregate.register(
                EmailAddress.of("founder@arka.com"),
                "hashed-secret",
                Set.of("ORG_OWNER"));

        when(securityRateLimitPort.ensureFounderRegistrationAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.existsByEmail(any())).thenReturn(Mono.just(false));
        when(passwordHashPort.hash(anyString())).thenReturn(Mono.just("hashed-secret"));
        when(userPersistencePort.create(any(), anyString(), anyString())).thenReturn(Mono.just(created));
        when(securityAuditPort.recordUserRegistered(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-1",
                "User",
                created.id().value(),
                "UserRegistered",
                "{}",
                "PENDING",
                Instant.now(),
                null,
                0,
                null,
                Instant.now(),
                Instant.now())));

        RegisterResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals(created.id().value(), result.userId());
        assertEquals("founder@arka.com", result.email());
        assertEquals("ACTIVE", result.status());
        verify(securityRateLimitPort).ensureFounderRegistrationAllowed(any(), any());
        verify(userPersistencePort).create(any(), anyString(), anyString());
        verify(outboxPersistencePort).store(any());
    }

    @Test
    void shouldRejectAdminCreateWhenActorContextIsMissing() {
        RegisterUseCase useCase = new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new UserRegistrationPolicy(),
                roleAssignmentPolicyPort,
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                new RegisterResultMapper());

        RegisterCommand command = new RegisterCommand(
                "user@arka.com",
                "PlainSecret123",
                RegistrationMode.ADMIN_CREATE,
                "ORG_USER",
                "",
                "JUnit",
                "127.0.0.1");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
    }

    @Test
    void shouldRejectAdminCreateWhenOrgAdminTriesToAssignOrgAdminRole() {
        RegisterUseCase useCase = new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new UserRegistrationPolicy(),
                roleAssignmentPolicyPort,
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                new RegisterResultMapper());

        RegisterCommand command = new RegisterCommand(
                "user@arka.com",
                "PlainSecret123",
                RegistrationMode.ADMIN_CREATE,
                "ORG_ADMIN",
                "actor-1",
                "JUnit",
                "127.0.0.1");

        when(roleAssignmentPolicyPort.canAssignRole(anyString(), anyString()))
                .thenReturn(Mono.just(false));

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command).block());
    }

    @Test
    void shouldAssignOrgUserByDefaultForAdminCreateWhenRoleIsNotProvided() {
        RegisterUseCase useCase = new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new UserRegistrationPolicy(),
                roleAssignmentPolicyPort,
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                new RegisterResultMapper());

        RegisterCommand command = new RegisterCommand(
                "new.user@arka.com",
                "PlainSecret123",
                RegistrationMode.ADMIN_CREATE,
                null,
                "owner-1",
                "JUnit",
                "127.0.0.1");

        UserAggregate created = UserAggregate.register(
                EmailAddress.of("new.user@arka.com"),
                "hashed-secret",
                Set.of("ORG_USER"));

        when(userPersistencePort.existsByEmail(any())).thenReturn(Mono.just(false));
        when(roleAssignmentPolicyPort.canAssignRole("owner-1", "ORG_USER")).thenReturn(Mono.just(true));
        when(passwordHashPort.hash(anyString())).thenReturn(Mono.just("hashed-secret"));
        when(userPersistencePort.create(any(), anyString(), anyString())).thenReturn(Mono.just(created));
        when(securityAuditPort.recordUserRegistered(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-2",
                "User",
                created.id().value(),
                "UserRegistered",
                "{}",
                "PENDING",
                Instant.now(),
                null,
                0,
                null,
                Instant.now(),
                Instant.now())));

        RegisterResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals("new.user@arka.com", result.email());
        verify(roleAssignmentPolicyPort).canAssignRole("owner-1", "ORG_USER");
        verify(userPersistencePort).create(any(), eq("ORG_USER"), eq("owner-1"));
        verifyNoInteractions(securityRateLimitPort);
    }

    @Test
    void shouldRejectFounderRegistrationWhenRateLimitIsExceeded() {
        RegisterUseCase useCase = new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new UserRegistrationPolicy(),
                roleAssignmentPolicyPort,
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                new RegisterResultMapper());

        RegisterCommand command = new RegisterCommand(
                "founder@arka.com",
                "PlainSecret123",
                RegistrationMode.ONBOARDING_OWNER,
                null,
                null,
                "JUnit",
                "127.0.0.1");

        when(securityRateLimitPort.ensureFounderRegistrationAllowed(any(), any()))
                .thenReturn(Mono.error(new RateLimitExceededException("Register-founder rate limit exceeded")));

        assertThrows(RateLimitExceededException.class, () -> useCase.handle(command).block());
        verifyNoInteractions(userPersistencePort, passwordHashPort, outboxPersistencePort);
    }
}
