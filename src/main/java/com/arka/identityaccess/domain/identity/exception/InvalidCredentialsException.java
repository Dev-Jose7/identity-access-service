package com.arka.identityaccess.domain.identity.exception;

import com.arka.identityaccess.domain.shared.exception.DomainException;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("credenciales_invalidas", "Credentials are invalid for the requested access context");
    }
}
