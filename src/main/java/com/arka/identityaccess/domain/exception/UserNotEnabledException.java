package com.arka.identityaccess.domain.exception;

public class UserNotEnabledException extends DomainException {

    public UserNotEnabledException() {
        super("usuario_no_habilitado", "User is not enabled to authenticate");
    }
}
