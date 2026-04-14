package com.arka.identityaccess.domain.session.exception;

import com.arka.identityaccess.domain.shared.exception.DomainException;

public class SessionAlreadyRevokedException extends DomainException {

    public SessionAlreadyRevokedException() {
        super("sesion_ya_revocada", "Session is already revoked or inactive");
    }
}
