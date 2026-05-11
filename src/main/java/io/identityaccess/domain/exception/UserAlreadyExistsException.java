package io.identityaccess.domain.exception;

public class UserAlreadyExistsException extends DomainException {

    public UserAlreadyExistsException() {
        super("usuario_ya_existe", "A user already exists with the provided email");
    }
}
