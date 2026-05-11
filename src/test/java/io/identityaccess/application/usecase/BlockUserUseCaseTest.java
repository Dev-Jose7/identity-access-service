package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.identityaccess.application.command.BlockUserCommand;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.BlockUserResult;
import io.identityaccess.application.usecase.command.BlockUserUseCase;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class BlockUserUseCaseTest {

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
    void shouldBlockUserAndPublishEventWhenStatusChanges() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, sessionPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        BlockUserCommand command = new BlockUserCommand("usr-10", "actor-1", "fraud");

        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.loadStatus(any()))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@example.test", "ACTIVE")));
        when(userPersistencePort.wouldBlockLastActiveExclusiveRoleHolder(any())).thenReturn(Mono.just(false));
        when(userPersistencePort.block(any(), eq(now)))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@example.test", "BLOCKED")));
        when(securityAuditPort.recordAccountBlocked("actor-1", "usr-10", "fraud", true)).thenReturn(Mono.empty());
        when(sessionPersistencePort.revokeActiveSessionsByUserId(any(), eq("ACCOUNT_BLOCKED"), eq(now)))
                .thenReturn(Mono.just(2L));
        when(securityAuditPort.recordSessionsRevoked("actor-1", "usr-10", "ACCOUNT_BLOCKED", 2L)).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-block-1",
                "User",
                "usr-10",
                "AccountBlocked",
                "{}",
                "PENDING",
                now,
                null,
                0,
                null,
                now,
                now)));

        BlockUserResult result = useCase.handle(command).block();

        assertEquals("usr-10", result.userId());
        assertEquals("BLOCKED", result.status());
        assertTrue(result.changed());
        verify(sessionPersistencePort).revokeActiveSessionsByUserId(any(), eq("ACCOUNT_BLOCKED"), eq(now));
        verify(outboxPersistencePort).store(any());
    }

    @Test
    void shouldBeIdempotentWhenUserIsAlreadyBlocked() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, sessionPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        BlockUserCommand command = new BlockUserCommand("usr-10", "actor-1", null);

        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.loadStatus(any()))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@example.test", "BLOCKED")));
        when(userPersistencePort.block(any(), eq(now)))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@example.test", "BLOCKED")));
        when(sessionPersistencePort.revokeActiveSessionsByUserId(any(), eq("ACCOUNT_BLOCKED"), eq(now)))
                .thenReturn(Mono.just(0L));
        when(securityAuditPort.recordSessionsRevoked("actor-1", "usr-10", "ACCOUNT_BLOCKED", 0L)).thenReturn(Mono.empty());
        when(securityAuditPort.recordAccountBlocked("actor-1", "usr-10", "ADMIN_BLOCK", false)).thenReturn(Mono.empty());

        BlockUserResult result = useCase.handle(command).block();

        assertEquals("usr-10", result.userId());
        assertEquals("BLOCKED", result.status());
        assertEquals(false, result.changed());
        verify(sessionPersistencePort).revokeActiveSessionsByUserId(any(), eq("ACCOUNT_BLOCKED"), eq(now));
        verify(outboxPersistencePort, never()).store(any());
    }

    @Test
    void shouldRejectBlockingLastActiveExclusiveRoleHolder() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, sessionPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);
        BlockUserCommand command = new BlockUserCommand("usr-root", "actor-1", "security");

        when(userPersistencePort.loadStatus(any()))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-root", "root@example.test", "ACTIVE")));
        when(userPersistencePort.wouldBlockLastActiveExclusiveRoleHolder(any())).thenReturn(Mono.just(true));

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command).block());
        verify(userPersistencePort, never()).block(any(), any());
        verifyNoAuditOrOutbox();
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, sessionPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);

        BlockUserCommand command = new BlockUserCommand("usr-10", " ", "fraud");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
        verify(userPersistencePort, never()).loadStatus(any());
    }

    private void verifyNoAuditOrOutbox() {
        verify(securityAuditPort, never()).recordAccountBlocked(any(), any(), any(), eq(true));
        verify(outboxPersistencePort, never()).store(any());
    }
}
