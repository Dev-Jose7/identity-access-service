package com.arka.identityaccess.domain.session.event;

import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import java.util.UUID;

public record SessionOpenedEvent(
        String eventId,
        Instant occurredAt,
        AccountId userId,
        SessionId sessionId,
        AccessJti accessJti,
        RefreshJti refreshJti)
        implements DomainEvent {

    public SessionOpenedEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("session opened event requires occurredAt");
        }
        if (userId == null || sessionId == null || accessJti == null || refreshJti == null) {
            throw new DomainInvariantViolationException("session opened event requires user, session and jti identifiers");
        }
    }

    public static SessionOpenedEvent create(
            AccountId userId,
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
    @Override public String aggregateType() { return "Session"; }
}
