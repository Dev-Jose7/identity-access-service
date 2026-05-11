package io.identityaccess.application.result;

import java.time.Instant;

public record TokenPairResult(
        String accessToken,
        String refreshToken,
        String sessionId,
        String tokenType,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {}
