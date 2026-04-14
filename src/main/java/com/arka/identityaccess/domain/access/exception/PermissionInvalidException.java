package com.arka.identityaccess.domain.access.exception;

import com.arka.identityaccess.domain.shared.exception.DomainException;

public class PermissionInvalidException extends DomainException {

    public PermissionInvalidException() {
        super("permiso_invalido", "Permission is invalid for this operation");
    }

    public PermissionInvalidException(String message) {
        super("permiso_invalido", message);
    }
}
