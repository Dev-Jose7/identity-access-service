package io.identityaccess.domain.model.role.event;

import io.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RoleDisabledEvent(
        String eventId,
        Instant occurredAt,
        String roleId,
        String roleCode,
        String actorUserId) implements DomainEvent {

    public static RoleDisabledEvent create(String roleId, String roleCode, String actorUserId, Instant occurredAt) {
        return new RoleDisabledEvent(UUID.randomUUID().toString(), occurredAt, roleId, normalizeRole(roleCode), normalize(actorUserId));
    }

    public RoleDisabledEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (roleId == null || roleId.isBlank()) throw new IllegalArgumentException("role id is required");
        roleCode = normalizeRole(roleCode);
        actorUserId = normalize(actorUserId);
    }

    @Override public String eventType() { return "RoleDisabled"; }
    @Override public String aggregateId() { return roleId; }
    @Override public Map<String, Object> payload() { return Map.of("roleId", roleId, "roleCode", roleCode, "actorUserId", actorUserId); }

    private static String normalizeRole(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("role code is required");
        return value.trim().toUpperCase();
    }

    private static String normalize(String value) { return value == null ? "" : value.trim(); }
}
