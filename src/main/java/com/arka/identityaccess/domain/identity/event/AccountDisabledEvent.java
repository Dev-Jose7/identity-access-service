package com.arka.identityaccess.domain.identity.event;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record AccountDisabledEvent(
        String eventId,
        Instant occurredAt,
        AccountId accountId,
        String reason)
        implements DomainEvent {

    public AccountDisabledEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || accountId == null) {
            throw new DomainInvariantViolationException("account disabled event is incomplete");
        }
        reason = reason == null ? "" : reason.trim();
    }

    public static AccountDisabledEvent create(AccountId accountId, String reason, Instant occurredAt) {
        return new AccountDisabledEvent(UUID.randomUUID().toString(), occurredAt, accountId, reason);
    }

    @Override
    public String eventType() {
        return "AccountDisabled";
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
