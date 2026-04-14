package com.arka.identityaccess.domain.identity.service;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;

public class CredentialPolicy {

    private final int minPasswordLength;

    public CredentialPolicy(int minPasswordLength) {
        if (minPasswordLength < 8) {
            throw new DomainInvariantViolationException("password min length must be at least 8");
        }
        this.minPasswordLength = minPasswordLength;
    }

    public int minPasswordLength() {
        return minPasswordLength;
    }

    public void ensureRawPasswordCompliant(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new DomainInvariantViolationException("password is required");
        }
        if (rawPassword.trim().length() < minPasswordLength) {
            throw new DomainInvariantViolationException("password does not satisfy minimum length");
        }
    }
}
