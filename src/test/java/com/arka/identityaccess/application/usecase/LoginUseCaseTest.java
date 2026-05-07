package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.arka.identityaccess.domain.exception.RateLimitExceededException;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.entity.UserCredential;
import com.arka.identityaccess.domain.model.user.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.model.user.enumtype.UserStatus;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.domain.service.PasswordPolicy;
import com.arka.identityaccess.domain.service.SessionPolicy;
import com.arka.identityaccess.domain.service.TokenPolicy;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
                EmailAddress.of("user@arka.com"),
                UserStatus.ACTIVE,
                new UserCredential("cred-1", UserId.of("usr-1"), EmailAddress.of("user@arka.com"), "hash", CredentialStatus.ACTIVE),
                List.of(),
                Set.of("ORG_OWNER"));

        LoginCommand command = new LoginCommand(
                "user@arka.com",
                "raw-secret",
                "Mozilla/5.0",
                "10.0.0.1");

        when(securityRateLimitPort.ensureLoginAllowed(any(), any())).thenReturn(Mono.empty());
        when(userPersistencePort.loadForLogin(any())).thenReturn(Mono.just(user));
        when(userPersistencePort.loadAuthorizationSnapshot(any()))
                .thenReturn(Mono.just(new UserPersistencePort.AuthorizationSnapshot(
                        "user@arka.com",
                        Set.of("ORG_OWNER"),
                        Set.of("iam.user.create", "iam.user.read"))));
        when(passwordHashPort.matches(anyString(), anyString())).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(sessionPersistencePort.create(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(jwtSigningPort.signAccessToken(any(), any(), any(), any())).thenReturn(Mono.just("access-token"));
        when(jwtSigningPort.signRefreshToken(any())).thenReturn(Mono.just("refresh-token"));
        when(securityAuditPort.recordLoginSuccess(any(), any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-1", "User", "usr-1", "UserLoggedIn", "{}", "PENDING", Instant.now(), null, 0, null, Instant.now(), Instant.now())));

        LoginResult result = useCase.handle(command).block();

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals("Bearer", result.tokenType());
        verify(outboxPersistencePort).store(any());
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
                "user@arka.com",
                "raw-secret",
                "Mozilla/5.0",
                "10.0.0.1");

        assertThrows(RateLimitExceededException.class, () -> useCase.handle(command).block());
        verifyNoInteractions(userPersistencePort, passwordHashPort, sessionPersistencePort, jwtSigningPort);
    }
}
