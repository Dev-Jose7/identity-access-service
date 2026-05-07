package com.arka.identityaccess.application.mapper.result;

import com.arka.identityaccess.application.result.RegisterResult;
import com.arka.identityaccess.domain.model.user.UserAggregate;
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
