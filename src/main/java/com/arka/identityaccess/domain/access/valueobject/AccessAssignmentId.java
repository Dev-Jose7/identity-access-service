package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;
import java.util.regex.Pattern;

public record AccessAssignmentId(String value) {

    private static final Pattern ASSIGNMENT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9-]{2,127}$");

    public AccessAssignmentId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("accessAssignmentId is required");
        }
        value = value.trim();
        if (!ASSIGNMENT_ID_PATTERN.matcher(value).matches()) {
            throw new DomainInvariantViolationException("accessAssignmentId format is invalid");
        }
    }

    public static AccessAssignmentId newId() {
        return new AccessAssignmentId(UUID.randomUUID().toString());
    }

    public static AccessAssignmentId of(String value) {
        return new AccessAssignmentId(value);
    }
}
