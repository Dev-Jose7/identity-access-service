package com.arka.identityaccess.domain.session.exception;

import com.arka.identityaccess.domain.shared.exception.DomainException;

public class SessionRevokedException extends DomainException {

    public SessionRevokedException() {
        super("token_expirado_o_revocado", "Session token is expired or revoked");
    }
}
