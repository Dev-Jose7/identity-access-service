package com.arka.identityaccess.domain.model.session.event;

import com.arka.identityaccess.domain.event.DomainEvent;
import com.arka.identityaccess.domain.model.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.model.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SessionRefreshedEvent(
        String eventId,
        Instant occurredAt,
        UserId userId,
        SessionId sessionId,
        AccessJti oldAccessJti,
        AccessJti newAccessJti,
        RefreshJti oldRefreshJti,
        RefreshJti newRefreshJti) implements DomainEvent {

    public static SessionRefreshedEvent create(UserId userId, SessionId sessionId, AccessJti oldAccessJti, AccessJti newAccessJti, RefreshJti oldRefreshJti, RefreshJti newRefreshJti, Instant occurredAt) {
        return new SessionRefreshedEvent(UUID.randomUUID().toString(), occurredAt, userId, sessionId, oldAccessJti, newAccessJti, oldRefreshJti, newRefreshJti);
    }
    @Override public String eventType() { return "SessionRefreshed"; }
    @Override public String aggregateId() { return sessionId.value(); }
    @Override public Map<String, Object> payload() {
        return Map.of(
            "userId", userId.value(),
            "sessionId", sessionId.value(),
            "oldAccessJti", oldAccessJti.value(),
            "newAccessJti", newAccessJti.value(),
            "oldRefreshJti", oldRefreshJti.value(),
            "newRefreshJti", newRefreshJti.value());
    }
}
