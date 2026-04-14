package com.arka.identityaccess.domain.session.event;

import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import java.util.UUID;

public record SessionRefreshedEvent(
        String eventId,
        Instant occurredAt,
        AccountId userId,
        SessionId sessionId,
        AccessJti oldAccessJti,
        AccessJti newAccessJti,
        RefreshJti oldRefreshJti,
        RefreshJti newRefreshJti) implements DomainEvent {

    public SessionRefreshedEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("session refreshed event requires occurredAt");
        }
        if (userId == null
                || sessionId == null
                || oldAccessJti == null
                || newAccessJti == null
                || oldRefreshJti == null
                || newRefreshJti == null) {
            throw new DomainInvariantViolationException("session refreshed event requires user, session and token identifiers");
        }
    }

    public static SessionRefreshedEvent create(AccountId userId, SessionId sessionId, AccessJti oldAccessJti, AccessJti newAccessJti, RefreshJti oldRefreshJti, RefreshJti newRefreshJti, Instant occurredAt) {
        return new SessionRefreshedEvent(UUID.randomUUID().toString(), occurredAt, userId, sessionId, oldAccessJti, newAccessJti, oldRefreshJti, newRefreshJti);
    }
    @Override public String eventType() { return "SessionRefreshed"; }
    @Override public String aggregateId() { return sessionId.value(); }
    @Override public String aggregateType() { return "Session"; }
}
