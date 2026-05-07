package com.arka.identityaccess.domain.model.user.entity;

import com.arka.identityaccess.domain.model.user.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import java.util.UUID;

public record UserCredential(
        String credentialId,
        UserId userId,
        EmailAddress email,
        String passwordHash,
        CredentialStatus status) {

    public UserCredential {
        if (credentialId == null || credentialId.isBlank()) {
            throw new IllegalArgumentException("credentialId is required");
        }
        if (userId == null || email == null || passwordHash == null || passwordHash.isBlank() || status == null) {
            throw new IllegalArgumentException("credential is incomplete");
        }
    }

    public static UserCredential primaryPassword(UserId userId, EmailAddress email, String passwordHash) {
        return new UserCredential(UUID.randomUUID().toString(), userId, email, passwordHash, CredentialStatus.ACTIVE);
    }

    public boolean isActive() {
        return status.isUsableForLogin();
    }
}
