package com.arka.identityaccess.domain.access.event;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record AccessProfileChangedEvent(
        String eventId,
        Instant occurredAt,
        AccountId accountId,
        String reason)
        implements DomainEvent {

    public AccessProfileChangedEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || accountId == null) {
            throw new DomainInvariantViolationException("access profile changed event is incomplete");
        }
        reason = reason == null ? "" : reason.trim();
    }

    public static AccessProfileChangedEvent create(AccountId accountId, String reason, Instant occurredAt) {
        return new AccessProfileChangedEvent(UUID.randomUUID().toString(), occurredAt, accountId, reason);
    }

    @Override
    public String eventType() {
        return "AccessProfileChanged";
    }

    @Override
    public String aggregateId() {
        return accountId.value();
    }

    @Override
    public String aggregateType() {
        return "AccountAccess";
    }
}
