package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.AssignRoleCommand;
import com.arka.identityaccess.application.port.in.AssignRoleCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.result.AssignRoleResult;
import com.arka.identityaccess.domain.access.aggregate.AccountAccessAggregate;
import com.arka.identityaccess.domain.access.entity.AccessAssignment;
import com.arka.identityaccess.domain.access.enumtype.AccessAssignmentStatus;
import com.arka.identityaccess.domain.access.service.AccessAssignmentPolicy;
import com.arka.identityaccess.domain.access.valueobject.AccountAccessContext;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.access.valueobject.RoleAssignmentContext;
import com.arka.identityaccess.domain.access.valueobject.AccessAssignmentId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import java.time.Instant;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AssignRoleUseCase implements AssignRoleCommandUseCase {

    private final UserPersistencePort userPersistencePort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ClockPort clockPort;
    private final AccessAssignmentPolicy accessAssignmentPolicy;

    public AssignRoleUseCase(
            UserPersistencePort userPersistencePort,
            SecurityAuditPort securityAuditPort,
            OutboxPersistencePort outboxPersistencePort,
            ClockPort clockPort,
            AccessAssignmentPolicy accessAssignmentPolicy) {
        this.userPersistencePort = userPersistencePort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.clockPort = clockPort;
        this.accessAssignmentPolicy = accessAssignmentPolicy;
    }

    @Override
    public Mono<AssignRoleResult> handle(AssignRoleCommand command) {
        AccountId targetUserId = AccountId.of(command.userId());
        String actorUserId = requireActor(command.actorUserId());
        RoleCode targetRoleCode = RoleCode.of(command.roleCode());
        AccountId actorId = AccountId.of(actorUserId);
        Instant now = clockPort.now();

        return userPersistencePort
                .canAssignRole(actorId, targetRoleCode)
                .flatMap(canAssign -> canAssign
                        ? Mono.empty()
                        : Mono.error(new OperationNotPermittedException("Actor cannot assign requested role")))
                .then(Mono.defer(() -> userPersistencePort.resolveRoleIdByCode(targetRoleCode)))
                .flatMap(targetRoleId -> assignRole(targetUserId, actorUserId, targetRoleCode, targetRoleId, now));
    }

    private Mono<AssignRoleResult> assignRole(
            AccountId targetUserId,
            String actorUserId,
            RoleCode targetRoleCode,
            RoleId targetRoleId,
            Instant now) {
        return Mono.zip(
                        userPersistencePort.loadStatus(targetUserId),
                        userPersistencePort.loadActiveAccessAssignments(targetUserId))
                .flatMap(tuple -> {
                    UserPersistencePort.UserStatusSnapshot statusSnapshot = tuple.getT1();
                    var activeAssignments = tuple.getT2();

                    accessAssignmentPolicy.ensureAssignable(
                            AccountAccessContext.of(
                                    targetUserId,
                                    AccountStatus.ACTIVE.name().equalsIgnoreCase(statusSnapshot.status())),
                            RoleAssignmentContext.of(targetRoleId, true, false));

                    AccountAccessAggregate accountAccess = AccountAccessAggregate.rehydrate(
                            targetUserId,
                            activeAssignments.stream()
                                    .map(this::toDomainAssignment)
                                    .toList());

                    AccountAccessAggregate.RoleAssignmentDecision decision =
                            accountAccess.assignRole(targetRoleId, actorUserId, now);
                    if (!decision.assigned()) {
                        return securityAuditPort
                                .recordRoleAssigned(actorUserId, targetUserId.value(), targetRoleCode.value(), false)
                                .thenReturn(new AssignRoleResult(
                                        targetUserId.value(),
                                        targetRoleCode.value(),
                                        false,
                                        "ALREADY_ASSIGNED"));
                    }

                    return userPersistencePort
                            .assignRole(targetUserId, targetRoleId, actorUserId, now)
                            .flatMap(assigned -> {
                                if (!assigned) {
                                    return securityAuditPort
                                            .recordRoleAssigned(actorUserId, targetUserId.value(), targetRoleCode.value(), false)
                                            .thenReturn(new AssignRoleResult(
                                                    targetUserId.value(),
                                                    targetRoleCode.value(),
                                                    false,
                                                    "ALREADY_ASSIGNED"));
                                }
                                return securityAuditPort
                                        .recordRoleAssigned(actorUserId, targetUserId.value(), targetRoleCode.value(), true)
                                        .then(publishDomainEvents(accountAccess.pullDomainEvents()))
                                        .thenReturn(new AssignRoleResult(
                                                targetUserId.value(),
                                                targetRoleCode.value(),
                                                true,
                                                "ASSIGNED"));
                            });
                });
    }

    private AccessAssignment toDomainAssignment(UserPersistencePort.ActiveAccessAssignment assignment) {
        return AccessAssignment.rehydrate(
                AccessAssignmentId.of(assignment.assignmentId()),
                assignment.accountId(),
                assignment.roleId(),
                AccessAssignmentStatus.ASSIGNED,
                assignment.assignedBy(),
                assignment.assignedAt(),
                null,
                null);
    }

    private Mono<Void> publishDomainEvents(Iterable<? extends DomainEvent> domainEvents) {
        return Flux.fromIterable(domainEvents).concatMap(outboxPersistencePort::store).then();
    }

    private String requireActor(String actorUserId) {
        if (actorUserId == null || actorUserId.isBlank()) {
            throw new OperationNotPermittedException("Authenticated actor is required");
        }
        return actorUserId.trim();
    }
}
