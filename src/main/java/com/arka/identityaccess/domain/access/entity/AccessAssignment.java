package com.arka.identityaccess.domain.access.entity;

import com.arka.identityaccess.domain.access.enumtype.AccessAssignmentStatus;
import com.arka.identityaccess.domain.access.valueobject.AccessAssignmentId;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public final class AccessAssignment {

    private final AccessAssignmentId id;
    private final AccountId accountId;
    private final RoleId roleId;
    private AccessAssignmentStatus status;
    private final String assignedBy;
    private final Instant assignedAt;
    private String revokedBy;
    private Instant revokedAt;

    private AccessAssignment(
            AccessAssignmentId id,
            AccountId accountId,
            RoleId roleId,
            AccessAssignmentStatus status,
            String assignedBy,
            Instant assignedAt,
            String revokedBy,
            Instant revokedAt) {
        if (id == null
                || accountId == null
                || roleId == null
                || status == null
                || assignedBy == null
                || assignedBy.isBlank()
                || assignedAt == null) {
            throw new DomainInvariantViolationException("access assignment is incomplete");
        }
        this.id = id;
        this.accountId = accountId;
        this.roleId = roleId;
        this.status = status;
        this.assignedBy = assignedBy;
        this.assignedAt = assignedAt;
        this.revokedBy = revokedBy;
        this.revokedAt = revokedAt;
    }

    public static AccessAssignment assign(
            AccountId accountId,
            RoleId roleId,
            String assignedBy,
            Instant occurredAt) {
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("role assignment requires occurredAt");
        }
        return new AccessAssignment(
                AccessAssignmentId.newId(),
                accountId,
                roleId,
                AccessAssignmentStatus.ASSIGNED,
                assignedBy.trim(),
                occurredAt,
                null,
                null);
    }

    public static AccessAssignment rehydrate(
            AccessAssignmentId id,
            AccountId accountId,
            RoleId roleId,
            AccessAssignmentStatus status,
            String assignedBy,
            Instant assignedAt,
            String revokedBy,
            Instant revokedAt) {
        return new AccessAssignment(id, accountId, roleId, status, assignedBy, assignedAt, revokedBy, revokedAt);
    }

    public boolean revoke(String actor, Instant occurredAt) {
        if (actor == null || actor.isBlank() || occurredAt == null) {
            throw new DomainInvariantViolationException("role revocation requires actor and occurredAt");
        }
        if (!status.isActive()) {
            return false;
        }
        status = AccessAssignmentStatus.REVOKED;
        revokedBy = actor.trim();
        revokedAt = occurredAt;
        return true;
    }

    public boolean isActive() {
        return status.isActive();
    }

    public AccessAssignmentId id() {
        return id;
    }

    public AccountId accountId() {
        return accountId;
    }

    public RoleId roleId() {
        return roleId;
    }

    public AccessAssignmentStatus status() {
        return status;
    }

    public String assignedBy() {
        return assignedBy;
    }

    public Instant assignedAt() {
        return assignedAt;
    }

    public String revokedBy() {
        return revokedBy;
    }

    public Instant revokedAt() {
        return revokedAt;
    }
}
