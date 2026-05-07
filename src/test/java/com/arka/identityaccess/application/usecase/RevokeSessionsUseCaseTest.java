package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import com.arka.identityaccess.domain.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.exception.UserNotFoundException;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
                .thenReturn(Mono.just(3L));
        when(securityAuditPort.recordSessionsRevokedByUser("actor-1", "usr-55", "ADMIN_REVOKE", 3L))
                .thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-revoke-1",
                "Session",
                "usr-55",
                "SessionsRevokedByUser",
                "{}",
                "PENDING",
                now,
                null,
                0,
                null,
                now,
                now)));

        RevokeSessionsResult result = useCase.handle(command).block();

        assertEquals("usr-55", result.userId());
        assertEquals(3L, result.revokedSessions());
        assertEquals("ADMIN_REVOKE", result.reason());
        assertEquals("COMPLETED", result.status());
        verify(outboxPersistencePort).store(any());
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
                .thenReturn(Mono.just(0L));
        when(securityAuditPort.recordSessionsRevokedByUser("actor-1", "usr-55", "MANUAL", 0L))
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

        assertThrows(UserNotFoundException.class, () -> useCase.handle(command).block());
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
}
