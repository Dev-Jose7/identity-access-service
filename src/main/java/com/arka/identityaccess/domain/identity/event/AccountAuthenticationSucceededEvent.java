package com.arka.identityaccess.domain.identity.event;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record AccountAuthenticationSucceededEvent(
        String eventId,
        Instant occurredAt,
        AccountId accountId,
        EmailAddress email,
        String attemptId)
        implements DomainEvent {

    public AccountAuthenticationSucceededEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || accountId == null || email == null || attemptId == null || attemptId.isBlank()) {
            throw new DomainInvariantViolationException("authentication succeeded event is incomplete");
        }
        attemptId = attemptId.trim();
    }

    public static AccountAuthenticationSucceededEvent create(
            AccountId accountId,
            EmailAddress email,
            String attemptId,
            Instant occurredAt) {
        return new AccountAuthenticationSucceededEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                accountId,
                email,
                attemptId);
    }

    @Override
    public String eventType() {
        return "AccountAuthenticationSucceeded";
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
