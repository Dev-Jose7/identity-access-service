package com.arka.identityaccess.domain.identity.entity;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;
import java.util.UUID;

public record AuthenticationAttempt(
        String attemptId,
        AccountId accountId,
        EmailAddress email,
        Instant occurredAt,
        String clientIp,
        boolean success,
        String failureCode) {

    public AuthenticationAttempt {
        if (attemptId == null || attemptId.isBlank()) {
            throw new DomainInvariantViolationException("attemptId is required");
        }
        if (email == null || occurredAt == null || clientIp == null || clientIp.isBlank()) {
            throw new DomainInvariantViolationException("authentication attempt is incomplete");
        }
        clientIp = clientIp.trim();
        failureCode = failureCode == null ? "" : failureCode.trim();
        if (success && !failureCode.isBlank()) {
            throw new DomainInvariantViolationException("successful attempt cannot have failureCode");
        }
        if (!success && failureCode.isBlank()) {
            throw new DomainInvariantViolationException("failed attempt requires failureCode");
        }
    }

    public static AuthenticationAttempt success(AccountId accountId, EmailAddress email, Instant occurredAt, String clientIp) {
        return new AuthenticationAttempt(UUID.randomUUID().toString(), accountId, email, occurredAt, clientIp, true, "");
    }

    public static AuthenticationAttempt failure(
            AccountId accountId,
            EmailAddress email,
            Instant occurredAt,
            String clientIp,
            String failureCode) {
        return new AuthenticationAttempt(UUID.randomUUID().toString(), accountId, email, occurredAt, clientIp, false, failureCode);
    }
}
