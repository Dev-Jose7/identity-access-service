package com.arka.identityaccess.domain.identity.event;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record AccountRegisteredEvent(
        String eventId,
        Instant occurredAt,
        AccountId accountId,
        EmailAddress email)
        implements DomainEvent {

    public AccountRegisteredEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || accountId == null || email == null) {
            throw new DomainInvariantViolationException("account registered event is incomplete");
        }
    }

    public static AccountRegisteredEvent create(AccountId accountId, EmailAddress email, Instant occurredAt) {
        return new AccountRegisteredEvent(UUID.randomUUID().toString(), occurredAt, accountId, email);
    }

    @Override
    public String eventType() {
        return "AccountRegistered";
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
