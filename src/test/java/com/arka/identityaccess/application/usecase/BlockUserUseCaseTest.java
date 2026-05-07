package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.command.BlockUserCommand;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.BlockUserResult;
import com.arka.identityaccess.application.usecase.command.BlockUserUseCase;
import com.arka.identityaccess.domain.exception.OperationNotPermittedException;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
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
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldBlockUserAndPublishEventWhenStatusChanges() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        BlockUserCommand command = new BlockUserCommand("usr-10", "actor-1", "fraud");

        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.loadStatus(any()))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@arka.com", "ACTIVE")));
        when(userPersistencePort.block(any(), eq(now)))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@arka.com", "BLOCKED")));
        when(securityAuditPort.recordUserBlocked("actor-1", "usr-10", "fraud", true)).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-block-1",
                "User",
                "usr-10",
                "UserBlocked",
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
        verify(outboxPersistencePort).store(any());
    }

    @Test
    void shouldBeIdempotentWhenUserIsAlreadyBlocked() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        BlockUserCommand command = new BlockUserCommand("usr-10", "actor-1", null);

        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.loadStatus(any()))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@arka.com", "BLOCKED")));
        when(userPersistencePort.block(any(), eq(now)))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-10", "user@arka.com", "BLOCKED")));
        when(securityAuditPort.recordUserBlocked("actor-1", "usr-10", "ADMIN_BLOCK", false)).thenReturn(Mono.empty());

        BlockUserResult result = useCase.handle(command).block();

        assertEquals("usr-10", result.userId());
        assertEquals("BLOCKED", result.status());
        assertEquals(false, result.changed());
        verify(outboxPersistencePort, never()).store(any());
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        BlockUserUseCase useCase = new BlockUserUseCase(userPersistencePort, securityAuditPort, outboxPersistencePort, clockPort);

        BlockUserCommand command = new BlockUserCommand("usr-10", " ", "fraud");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
        verify(userPersistencePort, never()).loadStatus(any());
    }
}
