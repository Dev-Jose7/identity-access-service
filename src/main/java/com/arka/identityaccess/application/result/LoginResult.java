package com.arka.identityaccess.application.result;

import java.time.Instant;

public record LoginResult(
        String accessToken,
        String refreshToken,
        String sessionId,
        String tokenType,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt) {}
