package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.identityaccess.application.command.UnblockUserCommand;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.UnblockUserResult;
import io.identityaccess.application.usecase.command.UnblockUserUseCase;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class UnblockUserUseCaseTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldUnblockBlockedUserAndPublishEvent() {
        UnblockUserUseCase useCase = new UnblockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        UnblockUserCommand command = new UnblockUserCommand("usr-10", "actor-1", "review-complete");

        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.loadStatus(any()))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@example.test", "BLOCKED")));
        when(userPersistencePort.unblock(any(), eq(now)))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@example.test", "ACTIVE")));
        when(securityAuditPort.recordAccountUnblocked("actor-1", "usr-10", "review-complete", true)).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-unblock-1",
                "User",
                "usr-10",
                "AccountUnblocked",
                "{}",
                "PENDING",
                now,
                null,
                0,
                null,
                now,
                now)));

        UnblockUserResult result = useCase.handle(command).block();

        assertEquals("usr-10", result.userId());
        assertEquals("ACTIVE", result.status());
        assertTrue(result.changed());
        verify(outboxPersistencePort).store(any());
    }

    @Test
    void shouldBeIdempotentWhenUserIsAlreadyActive() {
        UnblockUserUseCase useCase = new UnblockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        UnblockUserCommand command = new UnblockUserCommand("usr-10", "actor-1", null);

        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.loadStatus(any()))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@example.test", "ACTIVE")));
        when(userPersistencePort.unblock(any(), eq(now)))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@example.test", "ACTIVE")));
        when(securityAuditPort.recordAccountUnblocked("actor-1", "usr-10", "ADMIN_UNBLOCK", false)).thenReturn(Mono.empty());

        UnblockUserResult result = useCase.handle(command).block();

        assertEquals("ACTIVE", result.status());
        assertEquals(false, result.changed());
        verify(outboxPersistencePort, never()).store(any());
    }

    @Test
    void shouldRejectDisabledUser() {
        UnblockUserUseCase useCase = new UnblockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);
        UnblockUserCommand command = new UnblockUserCommand("usr-10", "actor-1", "review");

        when(userPersistencePort.loadStatus(any()))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@example.test", "DISABLED")));

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command).block());
        verify(userPersistencePort, never()).unblock(any(), any());
        verify(outboxPersistencePort, never()).store(any());
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        UnblockUserUseCase useCase = new UnblockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);
        UnblockUserCommand command = new UnblockUserCommand("usr-10", " ", "review");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
        verify(userPersistencePort, never()).loadStatus(any());
    }
}
