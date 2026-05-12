package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.identityaccess.application.command.RegisterCommand;
import io.identityaccess.application.mapper.command.RegisterCommandAssembler;
import io.identityaccess.application.mapper.result.RegisterResultMapper;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.RoleAssignmentPolicyPort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.port.out.security.PasswordHashPort;
import io.identityaccess.application.result.RegisterResult;
import io.identityaccess.application.usecase.command.RegisterUseCase;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.exception.PrimaryAccountAlreadyExistsException;
import io.identityaccess.domain.exception.RateLimitExceededException;
import io.identityaccess.domain.model.user.RegistrationMode;
import io.identityaccess.domain.model.user.UserAggregate;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import io.identityaccess.domain.service.UserRegistrationPolicy;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

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

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldCreatePublicAccountWithDefaultNonAdminRole() {
        RegisterUseCase useCase = newUseCase();
        RegisterCommand command = command(RegistrationMode.PUBLIC_SELF_REGISTRATION, null, null);
        UserAggregate created = UserAggregate.register(
                EmailAddress.of("register@example.test"),
                "hashed-secret",
                Set.of("ACCOUNT_USER"));

        when(securityRateLimitPort.ensureRegistrationAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.existsByEmail(any())).thenReturn(Mono.just(false));
        when(passwordHashPort.hash(anyString())).thenReturn(Mono.just("hashed-secret"));
        when(userPersistencePort.create(any(), eq("ACCOUNT_USER"), eq("SYSTEM_REGISTRATION"))).thenReturn(Mono.just(created));
        when(securityAuditPort.recordAccountRegistered(any())).thenReturn(Mono.empty());
        when(clockPort.now()).thenReturn(NOW);
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        RegisterResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals(created.id().value(), result.userId());
        assertEquals("register@example.test", result.email());
        assertEquals("ACTIVE", result.status());
        verify(securityRateLimitPort).ensureRegistrationAllowed(any(), any());
        verify(userPersistencePort, never()).existsByRoleCode(anyString());
        verifyNoInteractions(roleAssignmentPolicyPort);
    }

    @Test
    void shouldCreatePrimaryAccountWithProtectedInitialRoleWhenNoneExists() {
        RegisterUseCase useCase = newUseCase();
        RegisterCommand command = command(RegistrationMode.PRIMARY_REGISTRATION, null, null);
        UserAggregate created = UserAggregate.register(
                EmailAddress.of("register@example.test"),
                "hashed-secret",
                Set.of("SYSTEM_ADMIN"));

        when(securityRateLimitPort.ensureRegistrationAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.existsByRoleCode("SYSTEM_ADMIN")).thenReturn(Mono.just(false));
        when(userPersistencePort.existsByEmail(any())).thenReturn(Mono.just(false));
        when(passwordHashPort.hash(anyString())).thenReturn(Mono.just("hashed-secret"));
        when(userPersistencePort.create(any(), eq("SYSTEM_ADMIN"), eq("SYSTEM_PRIMARY_REGISTRATION"))).thenReturn(Mono.just(created));
        when(securityAuditPort.recordAccountRegistered(any())).thenReturn(Mono.empty());
        when(clockPort.now()).thenReturn(NOW);
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        RegisterResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals("register@example.test", result.email());
        verify(userPersistencePort).existsByRoleCode("SYSTEM_ADMIN");
        verifyNoInteractions(roleAssignmentPolicyPort);
    }

    @Test
    void shouldRejectPrimaryAccountWhenPrimaryRoleAlreadyExists() {
        RegisterUseCase useCase = newUseCase();
        RegisterCommand command = command(RegistrationMode.PRIMARY_REGISTRATION, null, null);

        when(securityRateLimitPort.ensureRegistrationAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.existsByRoleCode("SYSTEM_ADMIN")).thenReturn(Mono.just(true));

        assertThrows(PrimaryAccountAlreadyExistsException.class, () -> useCase.handle(command).block());
        verifyNoInteractions(passwordHashPort, securityAuditPort, outboxPersistencePort);
    }

    @Test
    void shouldRejectAdminCreateWhenActorContextIsMissing() {
        RegisterUseCase useCase = newUseCase();
        RegisterCommand command = command(RegistrationMode.ADMIN_CREATE, "ACCOUNT_USER", "");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
    }

    @Test
    void shouldRejectAdminCreateWhenActorCannotAssignRequestedRole() {
        RegisterUseCase useCase = newUseCase();
        RegisterCommand command = command(RegistrationMode.ADMIN_CREATE, "ACCESS_ADMIN", "actor-1");

        when(roleAssignmentPolicyPort.canAssignRole("actor-1", "ACCESS_ADMIN"))
                .thenReturn(Mono.just(false));

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command).block());
    }

    @Test
    void shouldAssignAccountUserByDefaultForAdminCreateWhenRoleIsNotProvided() {
        RegisterUseCase useCase = newUseCase();
        RegisterCommand command = command(RegistrationMode.ADMIN_CREATE, null, "owner-1");
        UserAggregate created = UserAggregate.register(
                EmailAddress.of("register@example.test"),
                "hashed-secret",
                Set.of("ACCOUNT_USER"));

        when(userPersistencePort.existsByEmail(any())).thenReturn(Mono.just(false));
        when(roleAssignmentPolicyPort.canAssignRole("owner-1", "ACCOUNT_USER")).thenReturn(Mono.just(true));
        when(passwordHashPort.hash(anyString())).thenReturn(Mono.just("hashed-secret"));
        when(userPersistencePort.create(any(), eq("ACCOUNT_USER"), eq("owner-1"))).thenReturn(Mono.just(created));
        when(securityAuditPort.recordAccountRegistered(any())).thenReturn(Mono.empty());
        when(clockPort.now()).thenReturn(NOW);
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        RegisterResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals("register@example.test", result.email());
        verify(roleAssignmentPolicyPort).canAssignRole("owner-1", "ACCOUNT_USER");
        verifyNoInteractions(securityRateLimitPort);
    }

    @Test
    void shouldRejectPublicRegistrationWhenRateLimitIsExceeded() {
        RegisterUseCase useCase = newUseCase();
        RegisterCommand command = command(RegistrationMode.PUBLIC_SELF_REGISTRATION, null, null);

        when(securityRateLimitPort.ensureRegistrationAllowed(any(), any()))
                .thenReturn(Mono.error(new RateLimitExceededException("Registration rate limit exceeded")));

        assertThrows(RateLimitExceededException.class, () -> useCase.handle(command).block());
        verifyNoInteractions(userPersistencePort, passwordHashPort, outboxPersistencePort);
    }

    private RegisterUseCase newUseCase() {
        return new RegisterUseCase(
                new RegisterCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                new UserRegistrationPolicy(),
                roleAssignmentPolicyPort,
                passwordHashPort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
                new RegisterResultMapper());
    }

    private RegisterCommand command(RegistrationMode mode, String roleCode, String actorId) {
        return new RegisterCommand(
                "register@example.test",
                "PlainSecret123",
                mode,
                roleCode,
                actorId,
                "JUnit",
                "127.0.0.1");
    }
}
