package io.identityaccess.domain.model.session.event;

import io.identityaccess.domain.event.DomainEvent;
import io.identityaccess.domain.model.session.valueobject.AccessJti;
import io.identityaccess.domain.model.session.valueobject.RefreshJti;
import io.identityaccess.domain.model.session.valueobject.SessionId;
import io.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SessionOpenedEvent(
        String eventId,
        Instant occurredAt,
        UserId userId,
        SessionId sessionId,
        AccessJti accessJti,
        RefreshJti refreshJti)
        implements DomainEvent {

    public SessionOpenedEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (userId == null || sessionId == null || accessJti == null || refreshJti == null) {
            throw new IllegalArgumentException("event data is incomplete");
        }
    }

    public static SessionOpenedEvent create(
            UserId userId,
            SessionId sessionId,
            AccessJti accessJti,
            RefreshJti refreshJti,
            Instant occurredAt) {
        return new SessionOpenedEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                userId,
                sessionId,
                accessJti,
                refreshJti);
    }

    @Override public String eventType() { return "SessionOpened"; }
    @Override public String aggregateId() { return sessionId.value(); }

    @Override public Map<String, Object> payload() {
        return Map.of(
                "accountId", userId.value(),
                "sessionId", sessionId.value(),
                "accessJti", accessJti.value(),
                "refreshJti", refreshJti.value());
    }
}
