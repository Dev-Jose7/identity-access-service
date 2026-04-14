package com.arka.identityaccess.domain.session.service;

import com.arka.identityaccess.domain.session.exception.SessionAlreadyRevokedException;
import com.arka.identityaccess.domain.session.exception.SessionRefreshNotAllowedException;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public class SessionPolicy {

    private final boolean rotateRefreshTokenOnRefresh;
    private final boolean rejectRefreshTokenReuse;
    private final boolean revokeAllSessionsOnCriticalAccessChange;

    public SessionPolicy(
            boolean rotateRefreshTokenOnRefresh,
            boolean rejectRefreshTokenReuse,
            boolean revokeAllSessionsOnCriticalAccessChange) {
        if (rejectRefreshTokenReuse && !rotateRefreshTokenOnRefresh) {
            throw new DomainInvariantViolationException(
                    "refresh token reuse rejection requires rotation to be enabled");
        }
        this.rotateRefreshTokenOnRefresh = rotateRefreshTokenOnRefresh;
        this.rejectRefreshTokenReuse = rejectRefreshTokenReuse;
        this.revokeAllSessionsOnCriticalAccessChange = revokeAllSessionsOnCriticalAccessChange;
    }

    public SessionPolicy() {
        this(true, true, false);
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

    public boolean shouldRotateRefreshTokenOnRefresh() {
        return rotateRefreshTokenOnRefresh;
    }

    public boolean shouldRejectRefreshTokenReuse() {
        return rejectRefreshTokenReuse;
    }

    public boolean shouldRevokeAllSessionsOnCriticalAccessChange() {
        return revokeAllSessionsOnCriticalAccessChange;
    }
}
