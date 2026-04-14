package com.arka.identityaccess.domain.access.aggregate;

import com.arka.identityaccess.domain.access.entity.AccessAssignment;
import com.arka.identityaccess.domain.access.event.AccessProfileChangedEvent;
import com.arka.identityaccess.domain.access.event.RoleAssignedToAccountEvent;
import com.arka.identityaccess.domain.access.event.RoleRevokedFromAccountEvent;
import com.arka.identityaccess.domain.access.valueobject.AccessAssignmentId;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountAccessAggregate {

    private final AccountId accountId;
    private final Map<RoleId, AccessAssignment> assignmentsByRole;
    private final List<DomainEvent> domainEvents;

    private AccountAccessAggregate(
            AccountId accountId,
            Map<RoleId, AccessAssignment> assignmentsByRole,
            List<DomainEvent> domainEvents) {
        if (accountId == null) {
            throw new DomainInvariantViolationException("account access requires accountId");
        }
        this.accountId = accountId;
        this.assignmentsByRole = new LinkedHashMap<>(assignmentsByRole == null ? Map.of() : assignmentsByRole);
        this.domainEvents = new ArrayList<>(domainEvents == null ? List.of() : domainEvents);
        ensureNoDuplicateActiveAssignments();
    }

    public static AccountAccessAggregate empty(AccountId accountId) {
        return new AccountAccessAggregate(accountId, Map.of(), List.of());
    }

    public static AccountAccessAggregate rehydrate(AccountId accountId, List<AccessAssignment> assignments) {
        Map<RoleId, AccessAssignment> byRole = new LinkedHashMap<>();
        if (assignments != null) {
            for (AccessAssignment assignment : assignments) {
                if (assignment == null) {
                    continue;
                }
                if (!accountId.equals(assignment.accountId())) {
                    throw new DomainInvariantViolationException("assignment does not belong to aggregate account");
                }
                AccessAssignment existing = byRole.putIfAbsent(assignment.roleId(), assignment);
                if (existing != null && existing.isActive() && assignment.isActive()) {
                    throw new DomainInvariantViolationException("duplicate active role assignment detected");
                }
                if (existing != null && !existing.isActive() && assignment.isActive()) {
                    byRole.put(assignment.roleId(), assignment);
                }
            }
        }
        return new AccountAccessAggregate(accountId, byRole, List.of());
    }

    public RoleAssignmentDecision assignRole(RoleId roleId, String actor, Instant occurredAt) {
        if (roleId == null || actor == null || actor.isBlank() || occurredAt == null) {
            throw new DomainInvariantViolationException("role assignment requires roleId, actor and occurredAt");
        }
        AccessAssignment existing = assignmentsByRole.get(roleId);
        if (existing != null && existing.isActive()) {
            return RoleAssignmentDecision.alreadyAssigned(existing.id());
        }

        AccessAssignment assignment = AccessAssignment.assign(accountId, roleId, actor.trim(), occurredAt);
        assignmentsByRole.put(roleId, assignment);
        recordDomainEvent(RoleAssignedToAccountEvent.create(
                assignment.id(),
                accountId,
                roleId,
                actor.trim(),
                occurredAt));
        recordDomainEvent(AccessProfileChangedEvent.create(accountId, "ROLE_ASSIGNED", occurredAt));
        return RoleAssignmentDecision.assigned(assignment.id());
    }

    public RoleRevocationDecision revokeRole(RoleId roleId, String actor, Instant occurredAt) {
        if (roleId == null || actor == null || actor.isBlank() || occurredAt == null) {
            throw new DomainInvariantViolationException("role revocation requires roleId, actor and occurredAt");
        }
        AccessAssignment assignment = assignmentsByRole.get(roleId);
        if (assignment == null || !assignment.revoke(actor.trim(), occurredAt)) {
            return RoleRevocationDecision.notRevoked();
        }

        recordDomainEvent(RoleRevokedFromAccountEvent.create(
                assignment.id(),
                accountId,
                roleId,
                actor.trim(),
                occurredAt));
        recordDomainEvent(AccessProfileChangedEvent.create(accountId, "ROLE_REVOKED", occurredAt));
        return RoleRevocationDecision.revoked(assignment.id());
    }

    public boolean hasActiveRole(RoleId roleId) {
        AccessAssignment assignment = assignmentsByRole.get(roleId);
        return assignment != null && assignment.isActive();
    }

    public List<RoleId> resolveActiveRoles() {
        return assignmentsByRole.values().stream()
                .filter(AccessAssignment::isActive)
                .map(AccessAssignment::roleId)
                .toList();
    }

    public AccountId accountId() {
        return accountId;
    }

    public List<AccessAssignment> assignments() {
        return Collections.unmodifiableList(new ArrayList<>(assignmentsByRole.values()));
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    private void ensureNoDuplicateActiveAssignments() {
        Map<RoleId, Integer> activeAssignments = new LinkedHashMap<>();
        for (AccessAssignment assignment : assignmentsByRole.values()) {
            if (assignment.isActive()) {
                activeAssignments.merge(assignment.roleId(), 1, Integer::sum);
            }
        }
        boolean hasDuplicates = activeAssignments.values().stream().anyMatch(count -> count > 1);
        if (hasDuplicates) {
            throw new DomainInvariantViolationException("duplicate active role assignment detected");
        }
    }

    private void recordDomainEvent(DomainEvent event) {
        if (event != null) {
            domainEvents.add(event);
        }
    }

    public record RoleAssignmentDecision(boolean assigned, AccessAssignmentId assignmentId) {

        public static RoleAssignmentDecision assigned(AccessAssignmentId assignmentId) {
            return new RoleAssignmentDecision(true, assignmentId);
        }

        public static RoleAssignmentDecision alreadyAssigned(AccessAssignmentId assignmentId) {
            return new RoleAssignmentDecision(false, assignmentId);
        }
    }

    public record RoleRevocationDecision(boolean revoked, AccessAssignmentId assignmentId) {

        public static RoleRevocationDecision revoked(AccessAssignmentId assignmentId) {
            return new RoleRevocationDecision(true, assignmentId);
        }

        public static RoleRevocationDecision notRevoked() {
            return new RoleRevocationDecision(false, null);
        }
    }
}
