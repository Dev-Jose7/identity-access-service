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
import com.arka.identityaccess.domain.exception.RateLimitExceededException;
import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.model.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.model.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.domain.model.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.domain.service.SessionPolicy;
import com.arka.identityaccess.domain.service.TokenPolicy;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
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
                SessionId.of("ses-1"),
                UserId.of("usr-1"),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("acc-old"),
                RefreshJti.of("ref-old"),
                SessionTimestamps.of(now.minusSeconds(60), now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.ACTIVE,
                null);

        when(securityRateLimitPort.ensureRefreshAllowed(any(), any())).thenReturn(Mono.empty());
        when(assembler.toRefreshJti(any())).thenReturn(RefreshJti.of("ref-old"));
        when(sessionPersistencePort.findActiveByRefreshJti(any())).thenReturn(Mono.just(activeSession));
        when(clockPort.now()).thenReturn(now);
        when(sessionPersistencePort.update(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(userPersistencePort.loadAuthorizationSnapshot(any()))
                .thenReturn(Mono.just(new UserPersistencePort.AuthorizationSnapshot(
                        "user@arka.com",
                        Set.of("ORG_OWNER"),
                        Set.of("iam.user.create", "iam.user.read"))));
        when(jwtSigningPort.signAccessToken(any(), any(), any(), any())).thenReturn(Mono.just("access-new"));
        when(jwtSigningPort.signRefreshToken(any())).thenReturn(Mono.just("refresh-new"));
        when(securityAuditPort.recordSessionRefreshed(any())).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-2",
                "Session",
                "ses-1",
                "SessionRefreshed",
                "{}",
                "PENDING",
                now,
                null,
                0,
                null,
                now,
                now)));

        TokenPairResult result = useCase.handle(new RefreshSessionCommand("any-refresh-token")).block();

        assertNotNull(result);
        assertEquals("access-new", result.accessToken());
        assertEquals("refresh-new", result.refreshToken());
        assertEquals("ses-1", result.sessionId());
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
