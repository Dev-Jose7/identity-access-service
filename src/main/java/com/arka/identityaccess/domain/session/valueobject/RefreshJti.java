package com.arka.identityaccess.domain.session.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record RefreshJti(String value) {

    public RefreshJti {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("refreshJti is required");
        }
        value = value.trim();
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new DomainInvariantViolationException("refreshJti must be a valid UUID");
        }
    }

    public static RefreshJti newId() {
        return new RefreshJti(UUID.randomUUID().toString());
    }

    public static RefreshJti of(String value) {
        return new RefreshJti(value);
    }
}
