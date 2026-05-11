package io.identityaccess.domain.model.session.valueobject;

import java.util.UUID;

public record AccessJti(String value) {

    public AccessJti {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("accessJti is required");
        }
    }

    public static AccessJti newId() {
        return new AccessJti(UUID.randomUUID().toString());
    }

    public static AccessJti of(String value) {
        return new AccessJti(value.trim());
    }
}
