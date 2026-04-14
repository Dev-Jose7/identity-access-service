package com.arka.identityaccess.domain.access.event;

import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record RoleCreatedEvent(
        String eventId,
        Instant occurredAt,
        RoleId roleId,
        RoleCode roleCode)
        implements DomainEvent {

    public RoleCreatedEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || roleId == null || roleCode == null) {
            throw new DomainInvariantViolationException("role created event is incomplete");
        }
    }

    public static RoleCreatedEvent create(RoleId roleId, RoleCode roleCode, Instant occurredAt) {
        return new RoleCreatedEvent(UUID.randomUUID().toString(), occurredAt, roleId, roleCode);
    }

    @Override
    public String eventType() {
        return "RoleCreated";
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
