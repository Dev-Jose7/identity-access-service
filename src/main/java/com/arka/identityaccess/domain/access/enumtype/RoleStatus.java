package com.arka.identityaccess.domain.access.enumtype;

public enum RoleStatus {
    ACTIVE,
    DISABLED;

    public boolean isAssignable() {
        return this == ACTIVE;
    }
}
