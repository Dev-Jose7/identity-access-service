package io.identityaccess.domain.model.user.valueobject;

import java.util.Locale;
import java.util.Objects;

public record EmailAddress(String value) {

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!value.contains("@")) {
            throw new IllegalArgumentException("email must be valid");
        }
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }

    public String normalized() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EmailAddress that && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
