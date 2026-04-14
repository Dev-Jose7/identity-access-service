package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.regex.Pattern;

public record PermissionCode(String value) {

    private static final Pattern PERMISSION_CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_.:-]{2,127}$");

    public PermissionCode {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("permissionCode is required");
        }
        value = value.trim();
        if (!PERMISSION_CODE_PATTERN.matcher(value).matches()) {
            throw new DomainInvariantViolationException("permissionCode format is invalid");
        }
    }

    public static PermissionCode of(String value) {
        return new PermissionCode(value);
    }
}
