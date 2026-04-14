package com.arka.identityaccess.infrastructure.adapter.in.web.response;

public record RevokeSessionsResponse(
        String userId,
        long revokedSessions,
        String reason,
        String status) {}
