package io.identityaccess.domain.service;

import java.time.Duration;
import java.time.Instant;

public class TokenPolicy {

    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public TokenPolicy(Duration accessTokenTtl, Duration refreshTokenTtl) {
        if (accessTokenTtl == null || refreshTokenTtl == null) {
            throw new IllegalArgumentException("token TTLs are required");
        }
        if (accessTokenTtl.isNegative() || accessTokenTtl.isZero()) {
            throw new IllegalArgumentException("access token ttl must be positive");
        }
        if (refreshTokenTtl.isNegative() || refreshTokenTtl.isZero()) {
            throw new IllegalArgumentException("refresh token ttl must be positive");
        }
        if (!refreshTokenTtl.minus(accessTokenTtl).isPositive()) {
            throw new IllegalArgumentException("refresh token ttl must be greater than access token ttl");
        }
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public Duration accessTokenTtl() {
        return accessTokenTtl;
    }

    public Duration refreshTokenTtl() {
        return refreshTokenTtl;
    }

    public Instant calculateAccessExpiry(Instant now) {
        return now.plus(accessTokenTtl);
    }

    public Instant calculateRefreshExpiry(Instant now) {
        return now.plus(refreshTokenTtl);
    }
}
