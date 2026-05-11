package io.identityaccess.infrastructure.adapter.in.web.response;

import java.time.Instant;

public record TokenPairResponse(
        String accessToken,
        String refreshToken,
        String sessionId,
        String tokenType,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {}
