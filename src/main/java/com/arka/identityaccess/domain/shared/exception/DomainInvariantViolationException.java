package com.arka.identityaccess.domain.shared.exception;

public class DomainInvariantViolationException extends DomainException {

    public DomainInvariantViolationException(String message) {
        super("invariante_de_dominio_invalida", message);
    }

    public DomainInvariantViolationException(String message, Throwable cause) {
        super("invariante_de_dominio_invalida", message, cause);
    }
}
