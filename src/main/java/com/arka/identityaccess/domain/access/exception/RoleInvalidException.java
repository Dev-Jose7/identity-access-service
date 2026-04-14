package com.arka.identityaccess.domain.access.exception;

import com.arka.identityaccess.domain.shared.exception.DomainException;

public class RoleInvalidException extends DomainException {

    public RoleInvalidException() {
        super("rol_invalido", "Role is invalid for this operation");
    }

    public RoleInvalidException(String message) {
        super("rol_invalido", message);
    }
}
