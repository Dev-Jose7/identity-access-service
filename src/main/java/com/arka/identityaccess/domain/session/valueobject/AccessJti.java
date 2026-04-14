package com.arka.identityaccess.domain.session.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record AccessJti(String value) {

    public AccessJti {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("accessJti is required");
        }
        value = value.trim();
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new DomainInvariantViolationException("accessJti must be a valid UUID");
        }
    }

    public static AccessJti newId() {
        return new AccessJti(UUID.randomUUID().toString());
    }

    public static AccessJti of(String value) {
        return new AccessJti(value);
    }
}
