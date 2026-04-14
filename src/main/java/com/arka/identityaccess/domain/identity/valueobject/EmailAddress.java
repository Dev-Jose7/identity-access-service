package com.arka.identityaccess.domain.identity.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.Locale;
import java.util.regex.Pattern;

public record EmailAddress(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$",
            Pattern.CASE_INSENSITIVE);

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("email is required");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new DomainInvariantViolationException("email must be valid");
        }
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }

    public String normalized() {
        return value;
    }
}
