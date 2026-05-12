package io.identityaccess.domain.service;

import io.identityaccess.domain.exception.SessionAlreadyRevokedException;
import io.identityaccess.domain.exception.SessionRefreshNotAllowedException;
import io.identityaccess.domain.exception.UserNotEnabledException;
import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.session.enumtype.SessionStatus;
import io.identityaccess.domain.model.session.valueobject.SessionTimestamps;
import io.identityaccess.domain.model.user.UserAggregate;
import java.time.Instant;

public class SessionPolicy {

    public void ensureUserCanOpenSession(UserAggregate user) {
        if (user == null || !user.isLoginAllowed()) {
            throw new UserNotEnabledException();
        }
    }

    public void ensureSessionCanRefresh(SessionAggregate session, Instant now) {
        if (session == null || session.status() != SessionStatus.ACTIVE) {
            throw new SessionRefreshNotAllowedException();
        }
        if (!session.timestamps().refreshTokenExpiresAt().isAfter(now)) {
            throw new SessionRefreshNotAllowedException();
        }
    }

    public void ensureSessionCanLogout(SessionAggregate session) {
        if (session == null || session.status() != SessionStatus.ACTIVE) {
            throw new SessionAlreadyRevokedException();
        }
    }

    public SessionTimestamps createTimestamps(Instant now, TokenPolicy tokenPolicy) {
        return SessionTimestamps.of(now, tokenPolicy.calculateAccessExpiry(now), tokenPolicy.calculateRefreshExpiry(now));
    }

    public SessionTimestamps refreshTimestamps(Instant now, TokenPolicy tokenPolicy) {
        return SessionTimestamps.of(now, tokenPolicy.calculateAccessExpiry(now), tokenPolicy.calculateRefreshExpiry(now));
    }
}
