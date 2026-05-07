package com.arka.identityaccess.domain.model.session.valueobject;

import java.time.Instant;

public record SessionTimestamps(
        Instant createdAt,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {

    public SessionTimestamps {
        if (createdAt == null || accessTokenExpiresAt == null || refreshTokenExpiresAt == null) {
            throw new IllegalArgumentException("session timestamps are required");
        }
        if (!accessTokenExpiresAt.isAfter(createdAt)) {
            throw new IllegalArgumentException("access token expiry must be after creation time");
        }
        if (!refreshTokenExpiresAt.isAfter(accessTokenExpiresAt)) {
            throw new IllegalArgumentException("refresh token expiry must be after access expiry");
        }
    }

    public static SessionTimestamps of(
            Instant createdAt,
            Instant accessTokenExpiresAt,
            Instant refreshTokenExpiresAt) {
        return new SessionTimestamps(createdAt, accessTokenExpiresAt, refreshTokenExpiresAt);
    }
}
