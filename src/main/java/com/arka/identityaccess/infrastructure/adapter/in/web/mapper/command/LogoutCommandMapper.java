package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command;

import com.arka.identityaccess.application.command.LogoutCommand;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.LogoutRequest;
import org.springframework.stereotype.Component;

@Component
public class LogoutCommandMapper {
    public LogoutCommand toCommand(LogoutRequest request) {
        return new LogoutCommand(request.sessionId());
    }
}
