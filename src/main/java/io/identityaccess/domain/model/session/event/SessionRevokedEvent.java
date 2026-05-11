package io.identityaccess.domain.model.session.event;

import io.identityaccess.domain.event.DomainEvent;
import io.identityaccess.domain.model.session.valueobject.SessionId;
import io.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SessionRevokedEvent(String eventId, Instant occurredAt, UserId userId, SessionId sessionId, String reason) implements DomainEvent {
    public static SessionRevokedEvent create(UserId userId, SessionId sessionId, Instant occurredAt, String reason) {
        return new SessionRevokedEvent(UUID.randomUUID().toString(), occurredAt, userId, sessionId, reason);
    }
    @Override public String eventType() { return "SessionRevoked"; }
    @Override public String aggregateId() { return sessionId.value(); }
    @Override public Map<String, Object> payload() { return Map.of("userId", userId.value(), "sessionId", sessionId.value(), "reason", reason); }
}
