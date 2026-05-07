package com.arka.identityaccess.domain.model.session.enumtype;

public enum SessionStatus {
    ACTIVE,
    REVOKED,
    EXPIRED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
