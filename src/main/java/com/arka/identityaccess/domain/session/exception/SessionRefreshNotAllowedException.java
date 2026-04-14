package com.arka.identityaccess.domain.session.exception;

import com.arka.identityaccess.domain.shared.exception.DomainException;

public class SessionRefreshNotAllowedException extends DomainException {

    public SessionRefreshNotAllowedException() {
        super("refresh_no_permitido", "Session cannot be refreshed in its current state");
    }
}
