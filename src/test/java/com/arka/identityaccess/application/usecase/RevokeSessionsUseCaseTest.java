package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.command.RevokeSessionsCommand;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.RevokeSessionsResult;
import com.arka.identityaccess.application.usecase.command.RevokeSessionsUseCase;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.application.exception.AccountNotFoundException;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class RevokeSessionsUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private SessionPersistencePort sessionPersistencePort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldRevokeSessionsAndPublishEventWhenAnySessionWasRevoked() {
        RevokeSessionsUseCase useCase = new RevokeSessionsUseCase(
                userPersistencePort,
                sessionPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        RevokeSessionsCommand command = new RevokeSessionsCommand("usr-55", "actor-1", null);

        when(userPersistencePort.existsById(any())).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(now);
        when(sessionPersistencePort.revokeActiveSessionsByUserId(any(), eq("ADMIN_REVOKE"), eq(now)))
                .thenReturn(Flux.fromIterable(List.of(
                        revokedSession("11111111-1111-1111-1111-111111111111", "usr-55", now),
                        revokedSession("22222222-2222-2222-2222-222222222222", "usr-55", now),
                        revokedSession("33333333-3333-3333-3333-333333333333", "usr-55", now))));
        when(securityAuditPort.recordSessionsRevoked("actor-1", "usr-55", "ADMIN_REVOKE", 3L))
                .thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        RevokeSessionsResult result = useCase.handle(command).block();

        assertEquals("usr-55", result.userId());
        assertEquals(3L, result.revokedSessions());
        assertEquals("ADMIN_REVOKE", result.reason());
        assertEquals("COMPLETED", result.status());
        verify(outboxPersistencePort, times(3)).store(any());
    }

    @Test
    void shouldNotPublishEventWhenNoSessionsAreRevoked() {
        RevokeSessionsUseCase useCase = new RevokeSessionsUseCase(
                userPersistencePort,
                sessionPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        RevokeSessionsCommand command = new RevokeSessionsCommand("usr-55", "actor-1", "MANUAL");

        when(userPersistencePort.existsById(any())).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(now);
        when(sessionPersistencePort.revokeActiveSessionsByUserId(any(), eq("MANUAL"), eq(now)))
                .thenReturn(Flux.empty());
        when(securityAuditPort.recordSessionsRevoked("actor-1", "usr-55", "MANUAL", 0L))
                .thenReturn(Mono.empty());

        RevokeSessionsResult result = useCase.handle(command).block();

        assertEquals("usr-55", result.userId());
        assertEquals(0L, result.revokedSessions());
        assertEquals("MANUAL", result.reason());
        verify(outboxPersistencePort, never()).store(any());
    }

    @Test
    void shouldFailWhenTargetUserDoesNotExist() {
        RevokeSessionsUseCase useCase = new RevokeSessionsUseCase(
                userPersistencePort,
                sessionPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort);

        RevokeSessionsCommand command = new RevokeSessionsCommand("usr-55", "actor-1", "MANUAL");
        when(userPersistencePort.existsById(any())).thenReturn(Mono.just(false));

        assertThrows(AccountNotFoundException.class, () -> useCase.handle(command).block());
        verify(sessionPersistencePort, never()).revokeActiveSessionsByUserId(any(), any(), any());
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        RevokeSessionsUseCase useCase = new RevokeSessionsUseCase(
                userPersistencePort,
                sessionPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort);

        RevokeSessionsCommand command = new RevokeSessionsCommand("usr-55", "", "MANUAL");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
    }

    private SessionAggregate revokedSession(String sessionId, String userId, Instant now) {
        return SessionAggregate.rehydrate(
                SessionId.of(sessionId),
                AccountId.of(userId),
                ClientDevice.of("device-1", "MacBook", "desktop"),
                ClientIp.of("127.0.0.1"),
                AccessJti.of("11111111-1111-1111-1111-111111111111"),
                RefreshJti.of("22222222-2222-2222-2222-222222222222"),
                SessionTimestamps.of(now, now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.REVOKED);
    }
}
