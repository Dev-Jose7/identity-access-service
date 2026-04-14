package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.command.RefreshSessionCommand;
import com.arka.identityaccess.application.mapper.command.RefreshSessionCommandAssembler;
import com.arka.identityaccess.application.mapper.result.TokenPairResultMapper;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.application.result.TokenPairResult;
import com.arka.identityaccess.application.usecase.command.RefreshSessionUseCase;
import com.arka.identityaccess.application.exception.RateLimitExceededException;
import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.session.service.SessionPolicy;
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
class RefreshSessionUseCaseTest {

    @Mock
    private SecurityRateLimitPort securityRateLimitPort;

    @Mock
    private RefreshSessionCommandAssembler assembler;

    @Mock
    private SessionPersistencePort sessionPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Mock
    private JwtSigningPort jwtSigningPort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @Test
    void shouldRotateTokensAndPersistOutboxOnRefresh() {
        RefreshSessionUseCase useCase = new RefreshSessionUseCase(
                securityRateLimitPort,
                assembler,
                sessionPersistencePort,
                clockPort,
                new SessionPolicy(),
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7)),
                jwtSigningPort,
                securityAuditPort,
                outboxPersistencePort,
                new TokenPairResultMapper(),
                userPersistencePort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        SessionAggregate activeSession = SessionAggregate.rehydrate(
                SessionId.of("11111111-1111-1111-1111-111111111111"),
                AccountId.of("usr-1"),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("44444444-4444-4444-4444-444444444444"),
                RefreshJti.of("55555555-5555-5555-5555-555555555555"),
                SessionTimestamps.of(now.minusSeconds(60), now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.ACTIVE);

        when(securityRateLimitPort.ensureRefreshAllowed(any(), any())).thenReturn(Mono.empty());
        when(assembler.toRefreshJti(any())).thenReturn(Mono.just(RefreshJti.of("55555555-5555-5555-5555-555555555555")));
        when(sessionPersistencePort.findActiveByRefreshJti(any())).thenReturn(Mono.just(activeSession));
        when(clockPort.now()).thenReturn(now);
        when(sessionPersistencePort.update(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userPersistencePort.loadAuthorizationSnapshot(any()))
                .thenReturn(Mono.just(AccessProfile.of(
                        AccountId.of("usr-1"),
                        EmailAddress.of("user@arka.com"),
                        Set.of(RoleCode.of("ORG_OWNER")),
                        Set.of(PermissionCode.of("iam.user.create"), PermissionCode.of("iam.user.read")),
                        now)));
        when(jwtSigningPort.signAccessToken(any(), any())).thenReturn(Mono.just("access-new"));
        when(jwtSigningPort.signRefreshToken(any())).thenReturn(Mono.just("refresh-new"));
        when(securityAuditPort.recordSessionRefreshed(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        TokenPairResult result = useCase.handle(new RefreshSessionCommand("any-refresh-token")).block();

        assertNotNull(result);
        assertEquals("access-new", result.accessToken());
        assertEquals("refresh-new", result.refreshToken());
        assertEquals("11111111-1111-1111-1111-111111111111", result.sessionId());
        verify(sessionPersistencePort).update(any());
        verify(outboxPersistencePort).store(any());
    }

    @Test
    void shouldRejectRefreshWhenRateLimitIsExceeded() {
        RefreshSessionUseCase useCase = new RefreshSessionUseCase(
                securityRateLimitPort,
                assembler,
                sessionPersistencePort,
                clockPort,
                new SessionPolicy(),
                new TokenPolicy(Duration.ofMinutes(15), Duration.ofDays(7)),
                jwtSigningPort,
                securityAuditPort,
                outboxPersistencePort,
                new TokenPairResultMapper(),
                userPersistencePort);

        when(securityRateLimitPort.ensureRefreshAllowed(any(), any()))
                .thenReturn(Mono.error(new RateLimitExceededException("Refresh rate limit exceeded")));

        assertThrows(
                RateLimitExceededException.class,
                () -> useCase.handle(new RefreshSessionCommand("any-refresh-token", "10.0.0.1")).block());

        verifyNoInteractions(assembler, sessionPersistencePort, jwtSigningPort, outboxPersistencePort);
    }
}
