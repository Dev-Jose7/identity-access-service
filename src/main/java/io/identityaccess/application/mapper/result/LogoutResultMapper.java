package io.identityaccess.application.mapper.result;

import io.identityaccess.application.result.LogoutResult;
import io.identityaccess.domain.model.session.SessionAggregate;
import org.springframework.stereotype.Component;

@Component
public class LogoutResultMapper {
    public LogoutResult toResult(SessionAggregate session) {
        return new LogoutResult(session.id().value(), session.status().name());
    }
}
