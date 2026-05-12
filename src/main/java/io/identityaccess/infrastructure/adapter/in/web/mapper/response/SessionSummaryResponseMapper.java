package io.identityaccess.infrastructure.adapter.in.web.mapper.response;

import io.identityaccess.application.result.SessionSummaryResult;
import io.identityaccess.infrastructure.adapter.in.web.response.SessionSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class SessionSummaryResponseMapper {

    public SessionSummaryResponse toResponse(SessionSummaryResult result) {
        return new SessionSummaryResponse(
                result.sessionId(),
                result.userId(),
                result.status(),
                result.ipAddress(),
                result.deviceId(),
                result.deviceName(),
                result.deviceType(),
                result.issuedAt(),
                result.accessTokenExpiresAt(),
                result.refreshTokenExpiresAt(),
                result.lastSeenAt(),
                result.revokedAt(),
                result.revocationReason(),
                result.createdAt(),
                result.updatedAt());
    }
}
