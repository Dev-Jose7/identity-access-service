package io.identityaccess.application.result;

import java.time.Instant;

public record SessionSummaryResult(
        String sessionId,
        String userId,
        String status,
        String ipAddress,
        String deviceId,
        String deviceName,
        String deviceType,
        Instant issuedAt,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt,
        Instant lastSeenAt,
        Instant revokedAt,
        String revocationReason,
        Instant createdAt,
        Instant updatedAt) {}
