package com.arka.identityaccess.domain.access.event;

import com.arka.identityaccess.domain.access.valueobject.AccessAssignmentId;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record RoleRevokedFromAccountEvent(
        String eventId,
        Instant occurredAt,
        AccessAssignmentId assignmentId,
        AccountId accountId,
        RoleId roleId,
        String revokedBy)
        implements DomainEvent {

    public RoleRevokedFromAccountEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null
                || assignmentId == null
                || accountId == null
                || roleId == null
                || revokedBy == null
                || revokedBy.isBlank()) {
            throw new DomainInvariantViolationException("role revocation event is incomplete");
        }
        revokedBy = revokedBy.trim();
    }

    public static RoleRevokedFromAccountEvent create(
            AccessAssignmentId assignmentId,
            AccountId accountId,
            RoleId roleId,
            String revokedBy,
            Instant occurredAt) {
        return new RoleRevokedFromAccountEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                assignmentId,
                accountId,
                roleId,
                revokedBy);
    }

    @Override
    public String eventType() {
        return "RoleRevokedFromAccount";
    }

    @Override
    public String aggregateId() {
        return accountId.value();
    }

    @Override
    public String aggregateType() {
        return "AccountAccess";
    }
}
