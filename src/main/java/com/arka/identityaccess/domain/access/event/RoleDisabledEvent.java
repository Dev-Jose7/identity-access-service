package com.arka.identityaccess.domain.access.event;

import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record RoleDisabledEvent(
        String eventId,
        Instant occurredAt,
        RoleId roleId)
        implements DomainEvent {

    public RoleDisabledEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || roleId == null) {
            throw new DomainInvariantViolationException("role disabled event is incomplete");
        }
    }

    public static RoleDisabledEvent create(RoleId roleId, Instant occurredAt) {
        return new RoleDisabledEvent(UUID.randomUUID().toString(), occurredAt, roleId);
    }

    @Override
    public String eventType() {
        return "RoleDisabled";
    }

    @Override
    public String aggregateId() {
        return roleId.value();
    }

    @Override
    public String aggregateType() {
        return "Role";
    }
}
