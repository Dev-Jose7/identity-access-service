package com.arka.identityaccess.application.mapper.command;

import com.arka.identityaccess.application.command.LogoutCommand;
import com.arka.identityaccess.domain.model.session.valueobject.SessionId;
import org.springframework.stereotype.Component;

@Component
public class LogoutCommandAssembler {
    public SessionId toSessionId(LogoutCommand command) {
        return SessionId.of(command.sessionId());
    }
}
