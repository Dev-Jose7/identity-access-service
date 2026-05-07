package com.arka.identityaccess.domain.exception;

public class SessionAlreadyRevokedException extends DomainException {

    public SessionAlreadyRevokedException() {
        super("sesion_ya_revocada", "Session is already revoked or inactive");
    }
}
