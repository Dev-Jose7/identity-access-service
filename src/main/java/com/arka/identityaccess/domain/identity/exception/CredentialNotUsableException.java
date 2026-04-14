package com.arka.identityaccess.domain.identity.exception;

import com.arka.identityaccess.domain.shared.exception.DomainException;

public class CredentialNotUsableException extends DomainException {

    public CredentialNotUsableException() {
        super("credencial_no_utilizable", "Current account credential cannot be used for authentication");
    }
}
