package com.arka.identityaccess.domain.model.user.enumtype;

public enum UserStatus {
    ACTIVE,
    BLOCKED,
    DISABLED;

    public boolean canLogin() {
        return this == ACTIVE;
    }
}
