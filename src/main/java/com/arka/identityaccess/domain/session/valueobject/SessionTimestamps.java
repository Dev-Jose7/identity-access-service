package com.arka.identityaccess.domain.session.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.time.Instant;

public record SessionTimestamps(
        Instant createdAt,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {

    public SessionTimestamps {
        if (createdAt == null || accessTokenExpiresAt == null || refreshTokenExpiresAt == null) {
            throw new DomainInvariantViolationException("session timestamps are required");
        }
        if (!accessTokenExpiresAt.isAfter(createdAt)) {
            throw new DomainInvariantViolationException("access token expiry must be after creation time");
        }
        if (!refreshTokenExpiresAt.isAfter(accessTokenExpiresAt)) {
            throw new DomainInvariantViolationException("refresh token expiry must be after access expiry");
        }
    }

    public static SessionTimestamps of(
            Instant createdAt,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt) {
        return new SessionTimestamps(createdAt, accessTokenExpiresAt, refreshTokenExpiresAt);
    }
}
