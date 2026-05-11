package io.identityaccess.domain.model.role.event;

import io.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PermissionRevokedFromRoleEvent(
        String eventId,
        Instant occurredAt,
        String roleId,
        String permissionCode,
        String actorUserId) implements DomainEvent {

    public static PermissionRevokedFromRoleEvent create(String roleId, String permissionCode, String actorUserId, Instant occurredAt) {
        return new PermissionRevokedFromRoleEvent(UUID.randomUUID().toString(), occurredAt, roleId, normalizeCode(permissionCode), normalize(actorUserId));
    }

    public PermissionRevokedFromRoleEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (roleId == null || roleId.isBlank()) throw new IllegalArgumentException("role id is required");
        permissionCode = normalizeCode(permissionCode);
        actorUserId = normalize(actorUserId);
    }

    @Override public String eventType() { return "PermissionRevokedFromRole"; }
    @Override public String aggregateId() { return roleId; }
    @Override public Map<String, Object> payload() { return Map.of("roleId", roleId, "permissionCode", permissionCode, "actorUserId", actorUserId); }

    private static String normalizeCode(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("permission code is required");
        return value.trim().toLowerCase();
    }

    private static String normalize(String value) { return value == null ? "" : value.trim(); }
}
