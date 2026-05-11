package io.identityaccess.infrastructure.adapter.in.web.mapper.response;

import io.identityaccess.application.result.LogoutResult;
import io.identityaccess.infrastructure.adapter.in.web.response.LogoutResponse;
import org.springframework.stereotype.Component;

@Component
public class LogoutResponseMapper {
    public LogoutResponse toResponse(LogoutResult result) {
        return new LogoutResponse(result.sessionId(), result.status());
    }
}
