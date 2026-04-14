package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.Locale;
import java.util.regex.Pattern;

public record PermissionResource(String value) {

    private static final Pattern RESOURCE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_.-]{1,99}$");

    public PermissionResource {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("permission resource is required");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!RESOURCE_PATTERN.matcher(value).matches()) {
            throw new DomainInvariantViolationException("permission resource format is invalid");
        }
    }

    public static PermissionResource of(String value) {
        return new PermissionResource(value);
    }
}
