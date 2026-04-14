package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.command.LoginCommand;
import com.arka.identityaccess.application.mapper.command.LoginCommandAssembler;
import com.arka.identityaccess.application.mapper.result.LoginResultMapper;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.application.port.out.security.PasswordHashPort;
import com.arka.identityaccess.application.result.LoginResult;
import com.arka.identityaccess.application.usecase.command.LoginUseCase;
import com.arka.identityaccess.application.exception.RateLimitExceededException;
import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.entity.AccountCredential;
import com.arka.identityaccess.domain.identity.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import com.arka.identityaccess.domain.identity.exception.InvalidCredentialsException;
import com.arka.identityaccess.domain.identity.service.AuthenticationSecurityPolicy;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.session.service.TokenPolicy;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
                new AuthenticationSecurityPolicy(5),
                clockPort,
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7)),
                sessionPersistencePort,
                jwtSigningPort,
                securityAuditPort,
                outboxPersistencePort,
                new LoginResultMapper());

        AccountAggregate user = AccountAggregate.rehydrate(
                AccountId.of("usr-1"),
                EmailAddress.of("user@arka.com"),
                true,
                AccountStatus.ACTIVE,
                new AccountCredential("cred-1", AccountId.of("usr-1"), EmailAddress.of("user@arka.com"), "hash", CredentialStatus.ACTIVE),
                0);

        LoginCommand command = new LoginCommand(
                "user@arka.com",
                "raw-secret",
                "Mozilla/5.0",
                "10.0.0.1");

        when(securityRateLimitPort.ensureLoginAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.loadForLogin(any())).thenReturn(Mono.just(user));
        when(userPersistencePort.loadAuthorizationSnapshot(any()))
                .thenReturn(Mono.just(AccessProfile.of(
                        AccountId.of("usr-1"),
                        EmailAddress.of("user@arka.com"),
                        Set.of(RoleCode.of("ORG_OWNER")),
                        Set.of(PermissionCode.of("iam.user.create"), PermissionCode.of("iam.user.read")),
                        Instant.parse("2026-01-01T00:00:00Z"))));
        when(userPersistencePort.recordLoginAttempt(any(), any())).thenReturn(Mono.empty());
        when(passwordHashPort.matches(anyString(), anyString())).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(sessionPersistencePort.create(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(jwtSigningPort.signAccessToken(any(), any())).thenReturn(Mono.just("access-token"));
        when(jwtSigningPort.signRefreshToken(any())).thenReturn(Mono.just("refresh-token"));
        when(securityAuditPort.recordLoginSuccess(any(), any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        LoginResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals("Bearer", result.tokenType());
        verify(outboxPersistencePort).store(any());
        verify(userPersistencePort).recordLoginAttempt(any(), any());
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
            public Mono<Void> ensureFounderRegistrationAllowed(EmailAddress email, ClientIp clientIp) {
                return Mono.empty();
            }
        };

        LoginUseCase useCase = new LoginUseCase(
                new LoginCommandAssembler(),
                denyAllLoginAttempts,
                userPersistencePort,
                passwordHashPort,
                new AuthenticationSecurityPolicy(5),
                clockPort,
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7)),
                sessionPersistencePort,
                jwtSigningPort,
                securityAuditPort,
                outboxPersistencePort,
                new LoginResultMapper());

        LoginCommand command = new LoginCommand(
                "user@arka.com",
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
                new AuthenticationSecurityPolicy(5),
                clockPort,
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7)),
                sessionPersistencePort,
                jwtSigningPort,
                securityAuditPort,
                outboxPersistencePort,
                new LoginResultMapper());

        AccountAggregate user = AccountAggregate.rehydrate(
                AccountId.of("usr-2"),
                EmailAddress.of("user2@arka.com"),
                true,
                AccountStatus.ACTIVE,
                new AccountCredential("cred-2", AccountId.of("usr-2"), EmailAddress.of("user2@arka.com"), "hash", CredentialStatus.ACTIVE),
                0);

        LoginCommand command = new LoginCommand(
                "user2@arka.com",
                "wrong-password",
                "Mozilla/5.0",
                "10.0.0.2");

        when(securityRateLimitPort.ensureLoginAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.loadForLogin(any())).thenReturn(Mono.just(user));
        when(userPersistencePort.recordLoginAttempt(any(), any())).thenReturn(Mono.empty());
        when(passwordHashPort.matches(anyString(), anyString())).thenReturn(Mono.just(false));
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));

        assertThrows(InvalidCredentialsException.class, () -> useCase.handle(command).block());
        verify(userPersistencePort).recordLoginAttempt(any(), any());
        verify(sessionPersistencePort, never()).create(any());
        verifyNoInteractions(jwtSigningPort, securityAuditPort, outboxPersistencePort);
    }
}
