package com.arka.identityaccess.domain.model.user.valueobject;

import java.util.UUID;

public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("userId is required");
        }
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }

    public static UserId of(String value) {
        return new UserId(value.trim());
    }
}
