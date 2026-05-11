package io.identityaccess.domain.exception;

public class RateLimitExceededException extends DomainException {

    public RateLimitExceededException() {
        super("operacion_no_permitida", "Rate limit exceeded");
    }

    public RateLimitExceededException(String message) {
        super("operacion_no_permitida", message);
    }
}
