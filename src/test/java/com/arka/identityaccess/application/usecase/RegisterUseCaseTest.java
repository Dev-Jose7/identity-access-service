package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.command.RegisterCommand;
import com.arka.identityaccess.application.mapper.command.RegisterCommandAssembler;
import com.arka.identityaccess.application.mapper.result.RegisterResultMapper;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.port.out.security.PasswordHashPort;
import com.arka.identityaccess.application.result.RegisterResult;
import com.arka.identityaccess.application.usecase.command.RegisterUseCase;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.application.exception.RateLimitExceededException;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.identity.enumtype.RegistrationMode;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.access.service.InitialAccessProvisioningPolicy;
import java.time.Instant;
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
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldCreateFounderInPublicRegisterFounderFlow() {
        RegisterUseCase useCase = new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new InitialAccessProvisioningPolicy(),
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
                new RegisterResultMapper());

        RegisterCommand command = new RegisterCommand(
                "founder@arka.com",
                "PlainSecret123",
                RegistrationMode.ONBOARDING_OWNER,
                null,
                null,
                "JUnit",
                "127.0.0.1");

        AccountAggregate created = AccountAggregate.register(
                EmailAddress.of("founder@arka.com"),
                "hashed-secret",
                false,
                Instant.parse("2026-01-01T00:00:00Z"));

        when(securityRateLimitPort.ensureFounderRegistrationAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.existsByEmail(any())).thenReturn(Mono.just(false));
        when(passwordHashPort.hash(anyString())).thenReturn(Mono.just("hashed-secret"));
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(userPersistencePort.create(any(), any(), anyString())).thenReturn(Mono.just(created));
        when(securityAuditPort.recordUserRegistered(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        RegisterResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals(created.id().value(), result.userId());
        assertEquals("founder@arka.com", result.email());
        assertEquals("ACTIVE", result.status());
        verify(securityRateLimitPort).ensureFounderRegistrationAllowed(any(), any());
        verify(userPersistencePort).create(any(), any(), anyString());
        verify(outboxPersistencePort, times(2)).store(any());
    }

    @Test
    void shouldRejectAdminCreateWhenActorContextIsMissing() {
        RegisterUseCase useCase = new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new InitialAccessProvisioningPolicy(),
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
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
                new InitialAccessProvisioningPolicy(),
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
                new RegisterResultMapper());

        RegisterCommand command = new RegisterCommand(
                "user@arka.com",
                "PlainSecret123",
                RegistrationMode.ADMIN_CREATE,
                "ORG_ADMIN",
                "actor-1",
                "JUnit",
                "127.0.0.1");

        when(userPersistencePort.canAssignRole(AccountId.of("actor-1"), RoleCode.of("ORG_ADMIN")))
                .thenReturn(Mono.just(false));

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command).block());
    }

    @Test
    void shouldAssignOrgUserByDefaultForAdminCreateWhenRoleIsNotProvided() {
        RegisterUseCase useCase = new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new InitialAccessProvisioningPolicy(),
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
                new RegisterResultMapper());

        RegisterCommand command = new RegisterCommand(
                "new.user@arka.com",
                "PlainSecret123",
                RegistrationMode.ADMIN_CREATE,
                null,
                "owner-1",
                "JUnit",
                "127.0.0.1");

        AccountAggregate created = AccountAggregate.register(
                EmailAddress.of("new.user@arka.com"),
                "hashed-secret",
                false,
                Instant.parse("2026-01-01T00:00:00Z"));

        when(userPersistencePort.existsByEmail(any())).thenReturn(Mono.just(false));
        when(userPersistencePort.canAssignRole(AccountId.of("owner-1"), RoleCode.of("ORG_USER")))
                .thenReturn(Mono.just(true));
        when(passwordHashPort.hash(anyString())).thenReturn(Mono.just("hashed-secret"));
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(userPersistencePort.create(any(), any(), anyString())).thenReturn(Mono.just(created));
        when(securityAuditPort.recordUserRegistered(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        RegisterResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals("new.user@arka.com", result.email());
        verify(userPersistencePort).canAssignRole(AccountId.of("owner-1"), RoleCode.of("ORG_USER"));
        verify(userPersistencePort).create(any(), eq(RoleCode.of("ORG_USER")), eq("owner-1"));
        verifyNoInteractions(securityRateLimitPort);
    }

    @Test
    void shouldRejectFounderRegistrationWhenRateLimitIsExceeded() {
        RegisterUseCase useCase = new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new InitialAccessProvisioningPolicy(),
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
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
