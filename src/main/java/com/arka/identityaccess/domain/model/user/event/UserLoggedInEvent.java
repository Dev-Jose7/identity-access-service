package com.arka.identityaccess.domain.model.user.event;

import com.arka.identityaccess.domain.event.DomainEvent;
import com.arka.identityaccess.domain.model.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UserLoggedInEvent(
        String eventId,
        Instant occurredAt,
        UserId userId,
        SessionId sessionId,
        AccessJti accessJti,
        RefreshJti refreshJti)
        implements DomainEvent {

    public UserLoggedInEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) occurredAt = Instant.now();
        if (userId == null || sessionId == null || accessJti == null || refreshJti == null) {
            throw new IllegalArgumentException("event data is incomplete");
        }
    }

    public static UserLoggedInEvent create(
            UserId userId,
            SessionId sessionId,
            AccessJti accessJti,
            RefreshJti refreshJti,
            Instant occurredAt) {
        return new UserLoggedInEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                userId,
                sessionId,
                accessJti,
                refreshJti);
    }

    @Override public String eventType() { return "UserLoggedIn"; }
    @Override public String aggregateId() { return userId.value(); }
    @Override public Map<String, Object> payload() {
        return Map.of(
                "userId", userId.value(),
                "sessionId", sessionId.value(),
                "accessJti", accessJti.value(),
                "refreshJti", refreshJti.value());
    }
}
