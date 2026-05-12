package io.identityaccess.domain.model.user.event;

import io.identityaccess.domain.event.DomainEvent;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AccountUnblockedEvent(
        String eventId,
        Instant occurredAt,
        String userId,
        String unblockedBy,
        String reason) implements DomainEvent {

    public static AccountUnblockedEvent create(
            String userId,
            String unblockedBy,
            String reason,
            Instant occurredAt) {
        return new AccountUnblockedEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                userId,
                unblockedBy,
                reason == null ? "" : reason);
    }

    public AccountUnblockedEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("account id is required");
        unblockedBy = unblockedBy == null ? "" : unblockedBy.trim();
        reason = reason == null ? "" : reason.trim();
    }

    @Override public String eventType() { return "AccountUnblocked"; }
    @Override public String aggregateId() { return userId; }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "accountId", userId,
                "unblockedBy", unblockedBy,
                "reason", reason);
    }
}
