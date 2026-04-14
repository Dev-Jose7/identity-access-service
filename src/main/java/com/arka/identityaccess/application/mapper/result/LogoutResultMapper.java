package com.arka.identityaccess.application.mapper.result;

import com.arka.identityaccess.application.result.LogoutResult;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import org.springframework.stereotype.Component;

@Component
public class LogoutResultMapper {
    public LogoutResult toResult(SessionAggregate session) {
        return new LogoutResult(session.id().value(), session.status().name());
    }
}
