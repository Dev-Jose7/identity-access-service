package com.arka.identityaccess.domain.identity.enumtype;

public enum CredentialStatus {
    ACTIVE,
    EXPIRED,
    REVOKED,
    COMPROMISED;

    public boolean isUsable() {
        return this == ACTIVE;
    }
}
