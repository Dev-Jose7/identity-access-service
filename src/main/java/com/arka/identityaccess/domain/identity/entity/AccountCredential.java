package com.arka.identityaccess.domain.identity.entity;

import com.arka.identityaccess.domain.identity.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;

public record AccountCredential(
        String credentialId,
        AccountId accountId,
        EmailAddress email,
        String passwordHash,
        CredentialStatus status) {

    public AccountCredential {
        if (credentialId == null || credentialId.isBlank()) {
            throw new DomainInvariantViolationException("credentialId is required");
        }
        if (accountId == null || email == null || passwordHash == null || passwordHash.isBlank() || status == null) {
            throw new DomainInvariantViolationException("credential is incomplete");
        }
    }

    public static AccountCredential primaryPassword(AccountId accountId, EmailAddress email, String passwordHash) {
        return new AccountCredential(UUID.randomUUID().toString(), accountId, email, passwordHash, CredentialStatus.ACTIVE);
    }

    public boolean isUsable() {
        return status.isUsable();
    }

    public AccountCredential changePassword(String nextPasswordHash) {
        if (nextPasswordHash == null || nextPasswordHash.isBlank()) {
            throw new DomainInvariantViolationException("next password hash is required");
        }
        return new AccountCredential(credentialId, accountId, email, nextPasswordHash, CredentialStatus.ACTIVE);
    }

    public AccountCredential revoke() {
        return new AccountCredential(credentialId, accountId, email, passwordHash, CredentialStatus.REVOKED);
    }
}
