package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.Locale;
import java.util.regex.Pattern;

public record RoleCode(String value) {

    private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]{1,99}$");

    public RoleCode {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("roleCode is required");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!ROLE_CODE_PATTERN.matcher(value).matches()) {
            throw new DomainInvariantViolationException("roleCode format is invalid");
        }
    }

    public static RoleCode of(String value) {
        return new RoleCode(value);
    }
}
