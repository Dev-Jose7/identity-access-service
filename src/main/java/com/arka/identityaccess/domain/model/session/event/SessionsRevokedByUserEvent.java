package com.arka.identityaccess.domain.model.session.event;

import com.arka.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SessionsRevokedByUserEvent(
        String eventId,
        Instant occurredAt,
        String userId,
        String revokedBy,
        String reason,
        long revokedSessions) implements DomainEvent {

    public static SessionsRevokedByUserEvent create(
            String userId,
            String revokedBy,
            String reason,
            long revokedSessions,
            Instant occurredAt) {
        return new SessionsRevokedByUserEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                userId,
                revokedBy,
                reason,
                revokedSessions);
    }

    @Override
    public String eventType() {
        return "SessionsRevokedByUser";
    }

    @Override
    public String aggregateId() {
        return userId;
    }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "userId", userId,
                "revokedBy", revokedBy,
                "reason", reason == null ? "" : reason,
                "revokedSessions", revokedSessions);
    }
}
