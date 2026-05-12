package io.identityaccess.application.result;

public record RevokeSessionsResult(
        String userId,
        long revokedSessions,
        String reason,
        String status) {}
