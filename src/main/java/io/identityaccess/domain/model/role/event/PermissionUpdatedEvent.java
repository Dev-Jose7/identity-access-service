package io.identityaccess.domain.model.role.event;

import io.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PermissionUpdatedEvent(
        String eventId,
        Instant occurredAt,
        String permissionId,
        String permissionCode,
        String actorUserId) implements DomainEvent {

    public static PermissionUpdatedEvent create(String permissionId, String permissionCode, String actorUserId, Instant occurredAt) {
        return new PermissionUpdatedEvent(UUID.randomUUID().toString(), occurredAt, permissionId, normalizeCode(permissionCode), normalize(actorUserId));
    }

    public PermissionUpdatedEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (permissionId == null || permissionId.isBlank()) throw new IllegalArgumentException("permission id is required");
        permissionCode = normalizeCode(permissionCode);
        actorUserId = normalize(actorUserId);
    }

    @Override public String eventType() { return "PermissionUpdated"; }
    @Override public String aggregateId() { return permissionId; }
    @Override public Map<String, Object> payload() { return Map.of("permissionId", permissionId, "permissionCode", permissionCode, "actorUserId", actorUserId); }

    private static String normalizeCode(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("permission code is required");
        return value.trim().toLowerCase();
    }

    private static String normalize(String value) { return value == null ? "" : value.trim(); }
}
