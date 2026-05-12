package io.identityaccess.domain.exception;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException() {
        super("usuario_no_encontrado", "User does not exist");
    }
}
