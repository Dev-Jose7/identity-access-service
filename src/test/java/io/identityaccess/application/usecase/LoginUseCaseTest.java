package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.identityaccess.application.command.LoginCommand;
import io.identityaccess.application.mapper.command.LoginCommandAssembler;
import io.identityaccess.application.mapper.result.LoginResultMapper;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.port.out.security.JwtSigningPort;
import io.identityaccess.application.port.out.security.PasswordHashPort;
import io.identityaccess.application.result.LoginResult;
import io.identityaccess.application.usecase.command.LoginUseCase;
import io.identityaccess.domain.exception.InvalidCredentialsException;
import io.identityaccess.domain.exception.RateLimitExceededException;
import io.identityaccess.domain.model.user.UserAggregate;
import io.identityaccess.domain.model.user.entity.UserCredential;
import io.identityaccess.domain.model.user.entity.UserLoginAttempt;
import io.identityaccess.domain.model.user.enumtype.CredentialStatus;
import io.identityaccess.domain.model.user.enumtype.UserStatus;
import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import io.identityaccess.domain.model.user.valueobject.UserId;
import io.identityaccess.domain.service.PasswordPolicy;
import io.identityaccess.domain.service.SessionPolicy;
import io.identityaccess.domain.service.TokenPolicy;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private SecurityRateLimitPort securityRateLimitPort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordHashPort passwordHashPort;

    @Mock
    private ClockPort clockPort;

    @Mock
    private SessionPersistencePort sessionPersistencePort;

    @Mock
    private JwtSigningPort jwtSigningPort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Test
    void shouldReturnTokensForHappyPathLogin() {
        LoginUseCase useCase = new LoginUseCase(
                new LoginCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                passwordHashPort,
                clockPort,
                new SessionPolicy(),
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7)),
                sessionPersistencePort,
                jwtSigningPort,
                securityAuditPort,
                outboxPersistencePort,
                new LoginResultMapper(),
                new PasswordPolicy());

        UserAggregate user = new UserAggregate(
                UserId.of("usr-1"),
                EmailAddress.of("user@example.test"),
                UserStatus.ACTIVE,
                new UserCredential("cred-1", UserId.of("usr-1"), EmailAddress.of("user@example.test"), "hash", CredentialStatus.ACTIVE),
                List.of(),
                Set.of("SYSTEM_ADMIN"));

        LoginCommand command = new LoginCommand(
                "user@example.test",
                "raw-secret",
                "Mozilla/5.0",
                "10.0.0.1");

        when(securityRateLimitPort.ensureLoginAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.loadForLogin(any())).thenReturn(Mono.just(user));
        when(userPersistencePort.recordLoginAttempt(any())).thenReturn(Mono.empty());
        when(userPersistencePort.loadAuthorizationSnapshot(any()))
                .thenReturn(Mono.just(new UserPersistencePort.AuthorizationSnapshot(
                        "user@example.test",
                        Set.of("SYSTEM_ADMIN"),
                        Set.of("iam.account.create", "iam.account.read"))));
        when(passwordHashPort.matches(anyString(), anyString())).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(sessionPersistencePort.create(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(jwtSigningPort.signAccessToken(any(), any(), any(), any())).thenReturn(Mono.just("access-token"));
        when(jwtSigningPort.signRefreshToken(any())).thenReturn(Mono.just("refresh-token"));
        when(securityAuditPort.recordLoginSuccess(any(), any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-1", "User", "usr-1", "SessionOpened", "{}", "PENDING", Instant.now(), null, 0, null, Instant.now(), Instant.now())));

        LoginResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals("Bearer", result.tokenType());
        verify(outboxPersistencePort).store(any());
        ArgumentCaptor<UserLoginAttempt> attemptCaptor = ArgumentCaptor.forClass(UserLoginAttempt.class);
        verify(userPersistencePort).recordLoginAttempt(attemptCaptor.capture());
        assertEquals(true, attemptCaptor.getValue().success());
    }

    @Test
    void shouldRejectLoginWhenRateLimitIsExceeded() {
        SecurityRateLimitPort denyAllLoginAttempts = new SecurityRateLimitPort() {
            @Override
            public Mono<Void> ensureLoginAllowed(EmailAddress email, ClientIp clientIp) {
                return Mono.error(new RateLimitExceededException("Login rate limit exceeded"));
            }

            @Override
            public Mono<Void> ensureRefreshAllowed(String refreshToken, ClientIp clientIp) {
                return Mono.empty();
            }

            @Override
            public Mono<Void> ensureRegistrationAllowed(EmailAddress email, ClientIp clientIp) {
                return Mono.empty();
            }
        };

        LoginUseCase useCase = new LoginUseCase(
                new LoginCommandAssembler(),
                denyAllLoginAttempts,
                userPersistencePort,
                passwordHashPort,
                clockPort,
                new SessionPolicy(),
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7)),
                sessionPersistencePort,
                jwtSigningPort,
                securityAuditPort,
                outboxPersistencePort,
                new LoginResultMapper(),
                new PasswordPolicy());

        LoginCommand command = new LoginCommand(
                "user@example.test",
                "raw-secret",
                "Mozilla/5.0",
                "10.0.0.1");

        assertThrows(RateLimitExceededException.class, () -> useCase.handle(command).block());
        verifyNoInteractions(userPersistencePort, passwordHashPort, sessionPersistencePort, jwtSigningPort);
    }

    @Test
    void shouldPersistFailedAttemptAndRejectWhenPasswordDoesNotMatch() {
        LoginUseCase useCase = new LoginUseCase(
                new LoginCommandAssembler(),
                securityRateLimitPort,
                userPersistencePort,
                passwordHashPort,
                clockPort,
                new SessionPolicy(),
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7)),
                sessionPersistencePort,
                jwtSigningPort,
                securityAuditPort,
                outboxPersistencePort,
                new LoginResultMapper(),
                new PasswordPolicy());

        UserAggregate user = new UserAggregate(
                UserId.of("usr-1"),
                EmailAddress.of("user@example.test"),
                UserStatus.ACTIVE,
                new UserCredential("cred-1", UserId.of("usr-1"), EmailAddress.of("user@example.test"), "hash", CredentialStatus.ACTIVE),
                List.of(),
                Set.of("SYSTEM_ADMIN"));

        LoginCommand command = new LoginCommand(
                "user@example.test",
                "wrong-secret",
                "Mozilla/5.0",
                "10.0.0.1");

        when(securityRateLimitPort.ensureLoginAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.loadForLogin(any())).thenReturn(Mono.just(user));
        when(userPersistencePort.recordLoginAttempt(any())).thenReturn(Mono.empty());
        when(passwordHashPort.matches(anyString(), anyString())).thenReturn(Mono.just(false));
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));

        assertThrows(InvalidCredentialsException.class, () -> useCase.handle(command).block());

        ArgumentCaptor<UserLoginAttempt> attemptCaptor = ArgumentCaptor.forClass(UserLoginAttempt.class);
        verify(userPersistencePort).recordLoginAttempt(attemptCaptor.capture());
        assertEquals(false, attemptCaptor.getValue().success());
        assertEquals("usr-1", attemptCaptor.getValue().userId().value());
        verifyNoInteractions(sessionPersistencePort, jwtSigningPort, securityAuditPort, outboxPersistencePort);
    }
}
