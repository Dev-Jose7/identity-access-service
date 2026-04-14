package com.arka.identityaccess.domain.access.enumtype;

public enum PermissionStatus {
    ACTIVE,
    DISABLED;

    public boolean isGrantable() {
        return this == ACTIVE;
    }
}
