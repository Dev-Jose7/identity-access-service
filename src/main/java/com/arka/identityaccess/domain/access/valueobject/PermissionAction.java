package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.Locale;
import java.util.regex.Pattern;

public record PermissionAction(String value) {

    private static final Pattern ACTION_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_.-]{1,99}$");

    public PermissionAction {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("permission action is required");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!ACTION_PATTERN.matcher(value).matches()) {
            throw new DomainInvariantViolationException("permission action format is invalid");
        }
    }

    public static PermissionAction of(String value) {
        return new PermissionAction(value);
    }
}
