package com.arka.identityaccess.domain.session.event;

import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import java.util.UUID;

public record SessionRevokedEvent(String eventId, Instant occurredAt, AccountId userId, SessionId sessionId, String reason) implements DomainEvent {

    public SessionRevokedEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null) {
            throw new DomainInvariantViolationException("session revoked event requires occurredAt");
        }
        if (userId == null || sessionId == null) {
            throw new DomainInvariantViolationException("session revoked event requires user and session identifiers");
        }
        reason = reason == null ? "" : reason.trim();
    }

    public static SessionRevokedEvent create(AccountId userId, SessionId sessionId, Instant occurredAt, String reason) {
        return new SessionRevokedEvent(UUID.randomUUID().toString(), occurredAt, userId, sessionId, reason);
    }
    @Override public String eventType() { return "SessionRevoked"; }
    @Override public String aggregateId() { return sessionId.value(); }
    @Override public String aggregateType() { return "Session"; }
}
