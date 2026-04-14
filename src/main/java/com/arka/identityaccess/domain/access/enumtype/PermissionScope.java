package com.arka.identityaccess.domain.access.enumtype;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;

public enum PermissionScope {
    ORGANIZATION,
    GLOBAL;

    public static PermissionScope of(String value) {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("permission scope is required");
        }
        try {
            return PermissionScope.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new DomainInvariantViolationException("permission scope is invalid");
        }
    }
}
