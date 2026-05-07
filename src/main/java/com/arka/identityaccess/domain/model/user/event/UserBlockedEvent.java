package com.arka.identityaccess.domain.model.user.event;

import com.arka.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserBlockedEvent(
        String eventId,
        Instant occurredAt,
        String userId,
        String blockedBy,
        String reason) implements DomainEvent {

    public static UserBlockedEvent create(
            String userId,
            String blockedBy,
            String reason,
            Instant occurredAt) {
        return new UserBlockedEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                userId,
                blockedBy,
                reason);
    }

    @Override
    public String eventType() {
        return "UserBlocked";
    }

    @Override
    public String aggregateId() {
        return userId;
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "userId", userId,
                "blockedBy", blockedBy,
                "reason", reason == null ? "" : reason);
    }
}
