package io.identityaccess.application.mapper.result;

import io.identityaccess.application.result.RegisterResult;
import io.identityaccess.domain.model.user.UserAggregate;
import org.springframework.stereotype.Component;

@Component
public class RegisterResultMapper {
    public RegisterResult toResult(UserAggregate user) {
        return new RegisterResult(
                user.id().value(),
                user.email().value(),
                user.status().name());
    }
}
