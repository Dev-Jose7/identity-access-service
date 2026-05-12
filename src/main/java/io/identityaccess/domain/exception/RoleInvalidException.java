package io.identityaccess.domain.exception;

public class RoleInvalidException extends DomainException {

    public RoleInvalidException() {
        super("rol_invalido", "Role is invalid for this operation");
    }
}
