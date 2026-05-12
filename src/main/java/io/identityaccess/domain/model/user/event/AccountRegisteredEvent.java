package io.identityaccess.domain.model.user.event;

import io.identityaccess.domain.event.DomainEvent;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import io.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AccountRegisteredEvent(
        String eventId,
        Instant occurredAt,
        UserId userId,
        EmailAddress email)
        implements DomainEvent {

    public AccountRegisteredEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (userId == null || email == null) throw new IllegalArgumentException("event data is incomplete");
    }

    public static AccountRegisteredEvent create(UserId userId, EmailAddress email, Instant occurredAt) {
        return new AccountRegisteredEvent(UUID.randomUUID().toString(), occurredAt, userId, email);
    }

    @Override public String eventType() { return "AccountRegistered"; }
    @Override public String aggregateId() { return userId.value(); }

    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "accountId", userId.value(),
                "email", email.value());
    }
}
