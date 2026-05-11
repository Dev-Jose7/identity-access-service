package io.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.identityaccess.application.command.AssignRoleCommand;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.RoleAssignmentPolicyPort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.AssignRoleResult;
import io.identityaccess.application.usecase.command.AssignRoleUseCase;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.OutboxEventRow;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AssignRoleUseCaseTest {

    @Mock
    private RoleAssignmentPolicyPort roleAssignmentPolicyPort;

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private SecurityAuditPort securityAuditPort;

    @Mock
    private OutboxPersistencePort outboxPersistencePort;

    @Mock
    private ClockPort clockPort;

    @Test
    void shouldAssignRoleAndPublishEventWhenPolicyAllows() {
        AssignRoleUseCase useCase = new AssignRoleUseCase(
                roleAssignmentPolicyPort,
                userPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AssignRoleCommand command = new AssignRoleCommand("usr-2", "ACCESS_ADMIN", "actor-1");

        when(roleAssignmentPolicyPort.canAssignRole("actor-1", "ACCESS_ADMIN")).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.assignRole(any(), eq("ACCESS_ADMIN"), eq("actor-1"), eq(now)))
                .thenReturn(Mono.just(new UserPersistencePort.RoleAssignmentOutcome("usr-2", "ACCESS_ADMIN", true)));
        when(securityAuditPort.recordRoleAssignedToAccount("actor-1", "usr-2", "ACCESS_ADMIN", true)).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.just(new OutboxEventRow(
                "evt-assign-1",
                "User",
                "usr-2",
                "RoleAssignedToAccount",
                "{}",
                "PENDING",
                now,
                null,
                0,
                null,
                now,
                now)));

        AssignRoleResult result = useCase.handle(command).block();

        assertEquals("usr-2", result.userId());
        assertEquals("ACCESS_ADMIN", result.roleCode());
        assertTrue(result.assigned());
        assertEquals("ASSIGNED", result.status());
        verify(outboxPersistencePort).store(any());
    }

    @Test
    void shouldNotPublishEventWhenRoleAssignmentIsIdempotent() {
        AssignRoleUseCase useCase = new AssignRoleUseCase(
                roleAssignmentPolicyPort,
                userPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort);

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AssignRoleCommand command = new AssignRoleCommand("usr-2", "ACCOUNT_USER", "actor-1");

        when(roleAssignmentPolicyPort.canAssignRole("actor-1", "ACCOUNT_USER")).thenReturn(Mono.just(true));
        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.assignRole(any(), eq("ACCOUNT_USER"), eq("actor-1"), eq(now)))
                .thenReturn(Mono.just(new UserPersistencePort.RoleAssignmentOutcome("usr-2", "ACCOUNT_USER", false)));
        when(securityAuditPort.recordRoleAssignedToAccount("actor-1", "usr-2", "ACCOUNT_USER", false)).thenReturn(Mono.empty());

        AssignRoleResult result = useCase.handle(command).block();

        assertEquals("usr-2", result.userId());
        assertEquals("ACCOUNT_USER", result.roleCode());
        assertEquals("ALREADY_ASSIGNED", result.status());
        verify(outboxPersistencePort, never()).store(any());
    }

    @Test
    void shouldRejectWhenPolicyDeniesRoleAssignment() {
        AssignRoleUseCase useCase = new AssignRoleUseCase(
                roleAssignmentPolicyPort,
                userPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort);

        AssignRoleCommand command = new AssignRoleCommand("usr-2", "ACCESS_ADMIN", "actor-1");
        when(roleAssignmentPolicyPort.canAssignRole("actor-1", "ACCESS_ADMIN")).thenReturn(Mono.just(false));

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command).block());
        verify(userPersistencePort, never()).assignRole(any(), any(), any(), any());
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        AssignRoleUseCase useCase = new AssignRoleUseCase(
                roleAssignmentPolicyPort,
                userPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort);

        AssignRoleCommand command = new AssignRoleCommand("usr-2", "ACCESS_ADMIN", " ");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
    }
}
