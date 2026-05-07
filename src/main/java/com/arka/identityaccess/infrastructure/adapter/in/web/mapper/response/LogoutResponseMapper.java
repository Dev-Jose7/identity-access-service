package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response;

import com.arka.identityaccess.application.result.LogoutResult;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.LogoutResponse;
import org.springframework.stereotype.Component;

@Component
public class LogoutResponseMapper {
    public LogoutResponse toResponse(LogoutResult result) {
        return new LogoutResponse(result.sessionId(), result.status());
    }
}
