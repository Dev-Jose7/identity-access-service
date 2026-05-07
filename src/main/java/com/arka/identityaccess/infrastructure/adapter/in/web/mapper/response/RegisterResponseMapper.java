package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response;

import com.arka.identityaccess.application.result.RegisterResult;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.RegisterResponse;
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
