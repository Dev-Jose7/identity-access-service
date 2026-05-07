package com.arka.identityaccess.domain.exception;

public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("credenciales_invalidas", "Credentials are invalid for the requested access context");
    }
}
