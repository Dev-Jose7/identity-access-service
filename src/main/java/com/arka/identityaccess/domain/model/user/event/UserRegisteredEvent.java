package com.arka.identityaccess.domain.model.user.event;

import com.arka.identityaccess.domain.event.DomainEvent;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserRegisteredEvent(
        String eventId,
        Instant occurredAt,
        UserId userId,
        EmailAddress email)
        implements DomainEvent {
    public UserRegisteredEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) occurredAt = Instant.now();
    }
    public static UserRegisteredEvent create(UserId userId, EmailAddress email, Instant occurredAt) {
        return new UserRegisteredEvent(UUID.randomUUID().toString(), occurredAt, userId, email);
    }
    @Override public String eventType() { return "UserRegistered"; }
    @Override public String aggregateId() { return userId.value(); }
    @Override
    public Map<String, Object> payload() {
        return Map.of(
                "userId", userId.value(),
                "email", email.value());
    }
}
