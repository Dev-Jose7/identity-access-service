package com.arka.identityaccess.domain.exception;

public class SessionRevokedException extends DomainException {

    public SessionRevokedException() {
        super("token_expirado_o_revocado", "Session token is expired or revoked");
    }
}
