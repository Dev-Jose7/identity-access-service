package com.arka.identityaccess.infrastructure.adapter.in.web.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String sessionId,
        String tokenType,
        long accessTokenExpiresAtEpochSecond,
        long refreshTokenExpiresAtEpochSecond) {}
