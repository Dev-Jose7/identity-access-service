package com.arka.identityaccess.domain.identity.event;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record AccountBlockedEvent(
        String eventId,
        Instant occurredAt,
        AccountId accountId,
        String reason)
        implements DomainEvent {

    public AccountBlockedEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || accountId == null) {
            throw new DomainInvariantViolationException("account blocked event is incomplete");
        }
        reason = reason == null ? "" : reason.trim();
    }

    public static AccountBlockedEvent create(AccountId accountId, String reason, Instant occurredAt) {
        return new AccountBlockedEvent(UUID.randomUUID().toString(), occurredAt, accountId, reason);
    }

    @Override
    public String eventType() {
        return "AccountBlocked";
    }

    @Override
    public String aggregateId() {
        return accountId.value();
    }

    @Override
    public String aggregateType() {
        return "Account";
    }
}
