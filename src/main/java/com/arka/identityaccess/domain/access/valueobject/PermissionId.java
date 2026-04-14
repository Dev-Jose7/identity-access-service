package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;
import java.util.regex.Pattern;

public record PermissionId(String value) {

    private static final Pattern PERMISSION_ID_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9-]{2,127}$");

    public PermissionId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("permissionId is required");
        }
        value = value.trim();
        if (!PERMISSION_ID_PATTERN.matcher(value).matches()) {
            throw new DomainInvariantViolationException("permissionId format is invalid");
        }
    }

    public static PermissionId newId() {
        return new PermissionId(UUID.randomUUID().toString());
    }

    public static PermissionId of(String value) {
        return new PermissionId(value);
    }
}
