package io.identityaccess.domain.model.role.event;

import io.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RoleAssignedToAccountEvent(
        String eventId,
        Instant occurredAt,
        String userId,
        String roleCode,
        String assignedBy,
        boolean assigned) implements DomainEvent {

    public static RoleAssignedToAccountEvent create(
            String userId,
            String roleCode,
            String assignedBy,
            boolean assigned,
            Instant occurredAt) {
        return new RoleAssignedToAccountEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                userId,
                roleCode,
                assignedBy,
                assigned);
    }

    public RoleAssignedToAccountEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("account id is required");
        if (roleCode == null || roleCode.isBlank()) throw new IllegalArgumentException("role code is required");
        assignedBy = assignedBy == null ? "" : assignedBy.trim();
        roleCode = roleCode.trim().toUpperCase();
    }

    @Override public String eventType() { return "RoleAssignedToAccount"; }
    @Override public String aggregateId() { return userId; }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "accountId", userId,
                "roleCode", roleCode,
                "assignedBy", assignedBy,
                "assigned", assigned);
    }
}
