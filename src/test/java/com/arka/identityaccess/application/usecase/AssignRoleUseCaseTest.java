package com.arka.identityaccess.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.command.AssignRoleCommand;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.AssignRoleResult;
import com.arka.identityaccess.application.usecase.command.AssignRoleUseCase;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.access.service.AccessAssignmentPolicy;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class AssignRoleUseCaseTest {

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
                userPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
                new AccessAssignmentPolicy());

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AssignRoleCommand command = new AssignRoleCommand("usr-2", "ORG_ADMIN", "actor-1");

        when(userPersistencePort.canAssignRole(AccountId.of("actor-1"), RoleCode.of("ORG_ADMIN")))
                .thenReturn(Mono.just(true));
        when(userPersistencePort.resolveRoleIdByCode(RoleCode.of("ORG_ADMIN")))
                .thenReturn(Mono.just(RoleId.of("rol-admin")));
        when(userPersistencePort.loadStatus(AccountId.of("usr-2")))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-2", "user@arka.com", "ACTIVE")));
        when(userPersistencePort.loadActiveAccessAssignments(AccountId.of("usr-2"))).thenReturn(Mono.just(Set.of()));
        when(clockPort.now()).thenReturn(now);
        when(userPersistencePort.assignRole(any(), eq(RoleId.of("rol-admin")), eq("actor-1"), eq(now)))
                .thenReturn(Mono.just(true));
        when(securityAuditPort.recordRoleAssigned("actor-1", "usr-2", "ORG_ADMIN", true)).thenReturn(Mono.empty());
        when(outboxPersistencePort.store(any())).thenReturn(Mono.empty());

        AssignRoleResult result = useCase.handle(command).block();

        assertEquals("usr-2", result.userId());
        assertEquals("ORG_ADMIN", result.roleCode());
        assertTrue(result.assigned());
        assertEquals("ASSIGNED", result.status());
        verify(outboxPersistencePort, times(2)).store(any());
    }

    @Test
    void shouldNotPublishEventWhenRoleAssignmentIsIdempotent() {
        AssignRoleUseCase useCase = new AssignRoleUseCase(
                userPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
                new AccessAssignmentPolicy());

        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        AssignRoleCommand command = new AssignRoleCommand("usr-2", "ORG_USER", "actor-1");

        when(userPersistencePort.canAssignRole(AccountId.of("actor-1"), RoleCode.of("ORG_USER")))
                .thenReturn(Mono.just(true));
        when(userPersistencePort.resolveRoleIdByCode(RoleCode.of("ORG_USER")))
                .thenReturn(Mono.just(RoleId.of("rol-user")));
        when(userPersistencePort.loadStatus(AccountId.of("usr-2")))
                .thenReturn(Mono.just(new UserPersistencePort.UserStatusSnapshot("usr-2", "user@arka.com", "ACTIVE")));
        when(userPersistencePort.loadActiveAccessAssignments(AccountId.of("usr-2"))).thenReturn(Mono.just(Set.of(
                new UserPersistencePort.ActiveAccessAssignment(
                        "11111111-1111-1111-1111-111111111111",
                        AccountId.of("usr-2"),
                        RoleId.of("rol-user"),
                        "actor-1",
                        now.minusSeconds(10))
        )));
        when(clockPort.now()).thenReturn(now);
        when(securityAuditPort.recordRoleAssigned("actor-1", "usr-2", "ORG_USER", false)).thenReturn(Mono.empty());

        AssignRoleResult result = useCase.handle(command).block();

        assertEquals("usr-2", result.userId());
        assertEquals("ORG_USER", result.roleCode());
        assertEquals("ALREADY_ASSIGNED", result.status());
        verify(outboxPersistencePort, never()).store(any());
    }

    @Test
    void shouldRejectWhenPolicyDeniesRoleAssignment() {
        AssignRoleUseCase useCase = new AssignRoleUseCase(
                userPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
                new AccessAssignmentPolicy());

        AssignRoleCommand command = new AssignRoleCommand("usr-2", "ORG_ADMIN", "actor-1");
        when(clockPort.now()).thenReturn(Instant.parse("2026-01-01T00:00:00Z"));
        when(userPersistencePort.canAssignRole(any(), any())).thenReturn(Mono.just(false));

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command).block());
        verify(userPersistencePort, never()).assignRole(any(), any(), any(), any());
    }

    @Test
    void shouldRejectWhenActorContextIsMissing() {
        AssignRoleUseCase useCase = new AssignRoleUseCase(
                userPersistencePort,
                securityAuditPort,
                outboxPersistencePort,
                clockPort,
                new AccessAssignmentPolicy());

        AssignRoleCommand command = new AssignRoleCommand("usr-2", "ORG_ADMIN", " ");

        assertThrows(OperationNotPermittedException.class, () -> useCase.handle(command));
    }
}
