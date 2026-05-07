package com.arka.identityaccess.domain.model.user.enumtype;

public enum CredentialStatus {
    ACTIVE,
    ROTATED,
    COMPROMISED,
    REVOKED;

    public boolean isUsableForLogin() {
        return this == ACTIVE;
    }
}
