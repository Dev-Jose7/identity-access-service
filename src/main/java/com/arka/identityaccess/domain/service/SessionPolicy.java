package com.arka.identityaccess.domain.service;

import com.arka.identityaccess.domain.exception.SessionAlreadyRevokedException;
import com.arka.identityaccess.domain.exception.SessionRefreshNotAllowedException;
import com.arka.identityaccess.domain.exception.UserNotEnabledException;
import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.model.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.model.user.UserAggregate;
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
