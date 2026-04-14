package com.arka.identityaccess.domain.access.event;

import com.arka.identityaccess.domain.access.enumtype.PermissionScope;
import com.arka.identityaccess.domain.access.valueobject.PermissionAction;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.PermissionId;
import com.arka.identityaccess.domain.access.valueobject.PermissionResource;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record PermissionCreatedEvent(
        String eventId,
        Instant occurredAt,
        PermissionId permissionId,
        PermissionCode permissionCode,
        PermissionResource resource,
        PermissionAction action,
        PermissionScope scope)
        implements DomainEvent {

    public PermissionCreatedEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null
                || permissionId == null
                || permissionCode == null
                || resource == null
                || action == null
                || scope == null) {
            throw new DomainInvariantViolationException("permission created event is incomplete");
        }
    }

    public static PermissionCreatedEvent create(
            PermissionId permissionId,
            PermissionCode permissionCode,
            PermissionResource resource,
            PermissionAction action,
            PermissionScope scope,
            Instant occurredAt) {
        return new PermissionCreatedEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                permissionId,
                permissionCode,
                resource,
                action,
                scope);
    }

    @Override
    public String eventType() {
        return "PermissionCreated";
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
