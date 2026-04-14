package com.arka.identityaccess.application.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Rate limit exceeded");
    }

    public RateLimitExceededException(String message) {
        super(message);
    }

    public String errorCode() {
        return "operacion_no_permitida";
    }
}
