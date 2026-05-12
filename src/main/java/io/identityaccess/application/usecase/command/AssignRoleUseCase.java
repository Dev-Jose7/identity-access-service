package io.identityaccess.application.usecase.command;

import io.identityaccess.application.command.AssignRoleCommand;
import io.identityaccess.application.port.in.AssignRoleCommandUseCase;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.RoleAssignmentPolicyPort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.result.AssignRoleResult;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.model.role.RoleCode;
import io.identityaccess.domain.model.role.event.RoleAssignedToAccountEvent;
import io.identityaccess.domain.model.user.valueobject.UserId;
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
        String targetRoleCode = RoleCode.from(command.roleCode()).value();

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
                        .recordRoleAssignedToAccount(actorUserId, outcome.userId(), outcome.roleCode(), outcome.assigned())
                        .then(outcome.assigned()
                                ? outboxPersistencePort.store(RoleAssignedToAccountEvent.create(
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
