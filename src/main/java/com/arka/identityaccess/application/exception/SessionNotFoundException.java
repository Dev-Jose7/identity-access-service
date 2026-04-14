package com.arka.identityaccess.application.exception;

public class SessionNotFoundException extends ApplicationException {

    public SessionNotFoundException() {
        super("sesion_no_encontrada", "Session was not found for the provided credential");
    }
}
