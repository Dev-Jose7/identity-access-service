package com.arka.identityaccess.domain.identity.event;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record AccountEmailVerifiedEvent(
        String eventId,
        Instant occurredAt,
        AccountId accountId)
        implements DomainEvent {

    public AccountEmailVerifiedEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || accountId == null) {
            throw new DomainInvariantViolationException("account email verified event is incomplete");
        }
    }

    public static AccountEmailVerifiedEvent create(AccountId accountId, Instant occurredAt) {
        return new AccountEmailVerifiedEvent(UUID.randomUUID().toString(), occurredAt, accountId);
    }

    @Override
    public String eventType() {
        return "AccountEmailVerified";
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
