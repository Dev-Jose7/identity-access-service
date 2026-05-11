package io.identityaccess.domain.exception;

public class SessionNotFoundException extends DomainException {

    public SessionNotFoundException() {
        super("sesion_no_encontrada", "Session was not found for the provided credential");
    }
}
