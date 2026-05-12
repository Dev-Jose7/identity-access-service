package io.identityaccess.infrastructure.adapter.in.web.mapper.response;

import io.identityaccess.application.result.RegisterResult;
import io.identityaccess.infrastructure.adapter.in.web.response.RegisterResponse;
import org.springframework.stereotype.Component;

@Component
public class RegisterResponseMapper {
    public RegisterResponse toResponse(RegisterResult result) {
        return new RegisterResponse(
                result.userId(),
                result.email(),
                result.status());
    }
}
