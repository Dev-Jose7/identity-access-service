package io.identityaccess.domain.model.session.valueobject;

import java.util.UUID;

public record RefreshJti(String value) {

    public RefreshJti {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("refreshJti is required");
        }
    }

    public static RefreshJti newId() {
        return new RefreshJti(UUID.randomUUID().toString());
    }

    public static RefreshJti of(String value) {
        return new RefreshJti(value.trim());
    }
}
