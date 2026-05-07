package com.arka.identityaccess.domain.model.session.valueobject;

import java.util.UUID;

public record SessionId(String value) {

    public SessionId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("sessionId is required");
        }
    }

    public static SessionId newId() {
        return new SessionId(UUID.randomUUID().toString());
    }

    public static SessionId of(String value) {
        return new SessionId(value.trim());
    }
}
