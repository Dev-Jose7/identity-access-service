package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;
import java.util.regex.Pattern;

public record RoleId(String value) {

    private static final Pattern ROLE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9-]{2,127}$");

    public RoleId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("roleId is required");
        }
        value = value.trim();
        if (!ROLE_ID_PATTERN.matcher(value).matches()) {
            throw new DomainInvariantViolationException("roleId format is invalid");
        }
    }

    public static RoleId newId() {
        return new RoleId(UUID.randomUUID().toString());
    }

    public static RoleId of(String value) {
        return new RoleId(value);
    }
}
