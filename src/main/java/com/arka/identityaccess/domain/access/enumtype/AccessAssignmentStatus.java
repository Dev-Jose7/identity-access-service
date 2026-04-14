package com.arka.identityaccess.domain.access.enumtype;

public enum AccessAssignmentStatus {
    ASSIGNED,
    REVOKED;

    public boolean isActive() {
        return this == ASSIGNED;
    }
}
