package com.arka.identityaccess.domain.access.exception;

import com.arka.identityaccess.domain.shared.exception.DomainException;

public class AccessAssignmentException extends DomainException {

    public AccessAssignmentException(String message) {
        super("asignacion_acceso_invalida", message);
    }
}
