package com.arka.identityaccess.domain.identity.valueobject;

import com.arka.identityaccess.domain.identity.entity.AuthenticationAttempt;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;

public record AuthenticationResult(
        AuthenticationAttempt attempt,
        boolean success,
        String failureCode) {

    public AuthenticationResult {
        if (attempt == null) {
            throw new DomainInvariantViolationException("authentication result requires attempt");
        }
        failureCode = failureCode == null ? "" : failureCode.trim();
        if (success && !failureCode.isBlank()) {
            throw new DomainInvariantViolationException("successful authentication cannot include failureCode");
        }
        if (!success && failureCode.isBlank()) {
            throw new DomainInvariantViolationException("failed authentication requires failureCode");
        }
    }

    public static AuthenticationResult success(AuthenticationAttempt attempt) {
        return new AuthenticationResult(attempt, true, "");
    }

    public static AuthenticationResult failure(AuthenticationAttempt attempt, String failureCode) {
        return new AuthenticationResult(attempt, false, failureCode);
    }
}
