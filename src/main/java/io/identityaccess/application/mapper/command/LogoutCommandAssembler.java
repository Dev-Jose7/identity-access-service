package io.identityaccess.application.mapper.command;

import io.identityaccess.application.command.LogoutCommand;
import io.identityaccess.domain.model.session.valueobject.SessionId;
import org.springframework.stereotype.Component;

@Component
public class LogoutCommandAssembler {
    public SessionId toSessionId(LogoutCommand command) {
        return SessionId.of(command.sessionId());
    }
}
