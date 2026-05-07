package com.arka.identityaccess.domain.model.user.event;

import com.arka.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RoleAssignedEvent(
        String eventId,
        Instant occurredAt,
        String userId,
        String roleCode,
        String assignedBy,
        boolean assigned) implements DomainEvent {

    public static RoleAssignedEvent create(
            String userId,
            String roleCode,
            String assignedBy,
            boolean assigned,
            Instant occurredAt) {
        return new RoleAssignedEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                userId,
                roleCode,
                assignedBy,
                assigned);
    }

    @Override
    public String eventType() {
        return "RoleAssigned";
    }

    @Override
    public String aggregateId() {
        return userId;
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "userId", userId,
                "roleCode", roleCode,
                "assignedBy", assignedBy,
                "assigned", assigned);
    }
}
