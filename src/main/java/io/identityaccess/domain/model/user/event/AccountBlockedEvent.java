package io.identityaccess.domain.model.user.event;

import io.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AccountBlockedEvent(
        String eventId,
        Instant occurredAt,
        String userId,
        String blockedBy,
        String reason) implements DomainEvent {

    public static AccountBlockedEvent create(
            String userId,
            String blockedBy,
            String reason,
            Instant occurredAt) {
        return new AccountBlockedEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                userId,
                blockedBy,
                reason == null ? "" : reason);
    }

    public AccountBlockedEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("account id is required");
        blockedBy = blockedBy == null ? "" : blockedBy.trim();
        reason = reason == null ? "" : reason.trim();
    }

    @Override public String eventType() { return "AccountBlocked"; }
    @Override public String aggregateId() { return userId; }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "accountId", userId,
                "blockedBy", blockedBy,
                "reason", reason);
    }
}
