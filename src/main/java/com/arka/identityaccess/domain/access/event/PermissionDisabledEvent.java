package com.arka.identityaccess.domain.access.event;

import com.arka.identityaccess.domain.access.valueobject.PermissionId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record PermissionDisabledEvent(
        String eventId,
        Instant occurredAt,
        PermissionId permissionId)
        implements DomainEvent {

    public PermissionDisabledEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || permissionId == null) {
            throw new DomainInvariantViolationException("permission disabled event is incomplete");
        }
    }

    public static PermissionDisabledEvent create(PermissionId permissionId, Instant occurredAt) {
        return new PermissionDisabledEvent(UUID.randomUUID().toString(), occurredAt, permissionId);
    }

    @Override
    public String eventType() {
        return "PermissionDisabled";
    }

    @Override
    public String aggregateId() {
        return permissionId.value();
    }

    @Override
    public String aggregateType() {
        return "Permission";
    }
}
