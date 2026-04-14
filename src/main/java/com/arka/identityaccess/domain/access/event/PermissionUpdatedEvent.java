package com.arka.identityaccess.domain.access.event;

import com.arka.identityaccess.domain.access.valueobject.PermissionId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record PermissionUpdatedEvent(
        String eventId,
        Instant occurredAt,
        PermissionId permissionId,
        String displayName)
        implements DomainEvent {

    public PermissionUpdatedEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || permissionId == null || displayName == null || displayName.isBlank()) {
            throw new DomainInvariantViolationException("permission updated event is incomplete");
        }
        displayName = displayName.trim();
    }

    public static PermissionUpdatedEvent create(PermissionId permissionId, String displayName, Instant occurredAt) {
        return new PermissionUpdatedEvent(UUID.randomUUID().toString(), occurredAt, permissionId, displayName);
    }

    @Override
    public String eventType() {
        return "PermissionUpdated";
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
