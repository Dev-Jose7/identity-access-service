package com.arka.identityaccess.application.exception;

public class ApplicationException extends RuntimeException {

    private final String errorCode;

    public ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String errorCode() {
        return errorCode;
    }
}
