package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response;

import com.arka.identityaccess.application.result.RevokeSessionsResult;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.RevokeSessionsResponse;
import org.springframework.stereotype.Component;

@Component
public class RevokeSessionsResponseMapper {

    public RevokeSessionsResponse toResponse(RevokeSessionsResult result) {
        return new RevokeSessionsResponse(
                result.userId(),
                result.revokedSessions(),
                result.reason(),
                result.status());
    }
}
