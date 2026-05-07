package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.AssignRoleCommand;
import com.arka.identityaccess.application.port.in.AssignRoleCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.RoleAssignmentPolicyPort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.AssignRoleResult;
import com.arka.identityaccess.domain.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.model.role.RoleCode;
import com.arka.identityaccess.domain.model.user.event.RoleAssignedEvent;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AssignRoleUseCase implements AssignRoleCommandUseCase {

    private final RoleAssignmentPolicyPort roleAssignmentPolicyPort;
    private final UserPersistencePort userPersistencePort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ClockPort clockPort;

    public AssignRoleUseCase(
            RoleAssignmentPolicyPort roleAssignmentPolicyPort,
            UserPersistencePort userPersistencePort,
            SecurityAuditPort securityAuditPort,
            OutboxPersistencePort outboxPersistencePort,
            ClockPort clockPort) {
        this.roleAssignmentPolicyPort = roleAssignmentPolicyPort;
        this.userPersistencePort = userPersistencePort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.clockPort = clockPort;
    }

    @Override
    public Mono<AssignRoleResult> handle(AssignRoleCommand command) {
        UserId targetUserId = UserId.of(command.userId());
        String actorUserId = requireActor(command.actorUserId());
        String targetRoleCode = RoleCode.from(command.roleCode()).name();

        return roleAssignmentPolicyPort
                .canAssignRole(actorUserId, targetRoleCode)
                .flatMap(canAssign -> canAssign
                        ? Mono.empty()
                        : Mono.error(new OperationNotPermittedException("Actor cannot assign requested role")))
                .then(Mono.defer(() -> userPersistencePort.assignRole(
                        targetUserId,
                        targetRoleCode,
                        actorUserId,
                        clockPort.now())))
                .flatMap(outcome -> securityAuditPort
                        .recordRoleAssigned(actorUserId, outcome.userId(), outcome.roleCode(), outcome.assigned())
                        .then(outcome.assigned()
                                ? outboxPersistencePort.store(RoleAssignedEvent.create(
                                                outcome.userId(),
                                                outcome.roleCode(),
                                                actorUserId,
                                                true,
                                                clockPort.now()))
                                        .then()
                                : Mono.empty())
                        .thenReturn(new AssignRoleResult(
                                outcome.userId(),
                                outcome.roleCode(),
                                outcome.assigned(),
                                outcome.assigned() ? "ASSIGNED" : "ALREADY_ASSIGNED")));
    }

    private String requireActor(String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new OperationNotPermittedException("Authenticated actor is required");
        }
        return actorUserId.trim();
    }
}
