package com.arka.identityaccess.domain.access.event;

import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record PermissionRevokedFromRoleEvent(
        String eventId,
        Instant occurredAt,
        RoleId roleId,
        PermissionCode permissionCode)
        implements DomainEvent {

    public PermissionRevokedFromRoleEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || roleId == null || permissionCode == null) {
            throw new DomainInvariantViolationException("permission revoked event is incomplete");
        }
    }

    public static PermissionRevokedFromRoleEvent create(RoleId roleId, PermissionCode permissionCode, Instant occurredAt) {
        return new PermissionRevokedFromRoleEvent(UUID.randomUUID().toString(), occurredAt, roleId, permissionCode);
    }

    @Override
    public String eventType() {
        return "PermissionRevokedFromRole";
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
