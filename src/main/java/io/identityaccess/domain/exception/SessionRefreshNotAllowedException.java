package io.identityaccess.domain.exception;

public class SessionRefreshNotAllowedException extends DomainException {

    public SessionRefreshNotAllowedException() {
        super("refresh_no_permitido", "Session cannot be refreshed in its current state");
    }
}
