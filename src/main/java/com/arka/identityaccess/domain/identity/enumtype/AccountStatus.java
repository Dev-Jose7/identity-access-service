package com.arka.identityaccess.domain.identity.enumtype;

public enum AccountStatus {
    PENDING_VERIFICATION,
    ACTIVE,
    BLOCKED,
    DISABLED;

    public boolean canAuthenticate() {
        return this == ACTIVE;
    }

    public boolean isOperable() {
        return this == ACTIVE;
    }
}
