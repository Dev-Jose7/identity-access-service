package io.identityaccess.domain.exception;

public class PrimaryAccountAlreadyExistsException extends DomainException {

    public PrimaryAccountAlreadyExistsException() {
        super("cuenta_primaria_ya_existe", "Primary account is already registered");
    }
}
