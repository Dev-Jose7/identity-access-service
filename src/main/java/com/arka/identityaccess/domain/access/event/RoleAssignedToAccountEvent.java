package com.arka.identityaccess.domain.access.event;

import com.arka.identityaccess.domain.access.valueobject.AccessAssignmentId;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record RoleAssignedToAccountEvent(
        String eventId,
        Instant occurredAt,
        AccessAssignmentId assignmentId,
        AccountId accountId,
        RoleId roleId,
        String assignedBy)
        implements DomainEvent {

    public RoleAssignedToAccountEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null
                || assignmentId == null
                || accountId == null
                || roleId == null
                || assignedBy == null
                || assignedBy.isBlank()) {
            throw new DomainInvariantViolationException("role assignment event is incomplete");
        }
        assignedBy = assignedBy.trim();
    }

    public static RoleAssignedToAccountEvent create(
            AccessAssignmentId assignmentId,
            AccountId accountId,
            RoleId roleId,
            String assignedBy,
            Instant occurredAt) {
        return new RoleAssignedToAccountEvent(UUID.randomUUID().toString(), occurredAt, assignmentId, accountId, roleId, assignedBy);
    }

    @Override
    public String eventType() {
        return "RoleAssignedToAccount";
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
