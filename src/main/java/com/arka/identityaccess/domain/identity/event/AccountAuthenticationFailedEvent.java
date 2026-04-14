package com.arka.identityaccess.domain.identity.event;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record AccountAuthenticationFailedEvent(
        String eventId,
        Instant occurredAt,
        AccountId accountId,
        EmailAddress email,
        String attemptId,
        String failureCode)
        implements DomainEvent {

    public AccountAuthenticationFailedEvent {
        if (eventId == null || eventId.isBlank()) {
            eventId = UUID.randomUUID().toString();
        }
        if (occurredAt == null || email == null || attemptId == null || attemptId.isBlank()) {
            throw new DomainInvariantViolationException("authentication failed event is incomplete");
        }
        attemptId = attemptId.trim();
        failureCode = failureCode == null ? "" : failureCode.trim();
        if (failureCode.isBlank()) {
            throw new DomainInvariantViolationException("authentication failed event requires failureCode");
        }
    }

    public static AccountAuthenticationFailedEvent create(
            AccountId accountId,
            EmailAddress email,
            String attemptId,
            String failureCode,
            Instant occurredAt) {
        return new AccountAuthenticationFailedEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                accountId,
                email,
                attemptId,
                failureCode);
    }

    @Override
    public String eventType() {
        return "AccountAuthenticationFailed";
    }

    @Override
    public String aggregateId() {
        return accountId == null ? "ANONYMOUS" : accountId.value();
    }

    @Override
    public String aggregateType() {
        return "Account";
    }
}
