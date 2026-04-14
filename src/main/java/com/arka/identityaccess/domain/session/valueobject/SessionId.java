package com.arka.identityaccess.domain.session.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record SessionId(String value) {

    public SessionId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("sessionId is required");
        }
        value = value.trim();
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new DomainInvariantViolationException("sessionId must be a valid UUID");
        }
    }

    public static SessionId newId() {
        return new SessionId(UUID.randomUUID().toString());
    }

    public static SessionId of(String value) {
        return new SessionId(value);
    }
}
