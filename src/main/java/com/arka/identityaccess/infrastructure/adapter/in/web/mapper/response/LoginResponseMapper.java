package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response;

import com.arka.identityaccess.application.result.LoginResult;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.LoginResponse;
import org.springframework.stereotype.Component;

@Component
public class LoginResponseMapper {

    public LoginResponse toResponse(LoginResult result) {
        return new LoginResponse(
                result.accessToken(),
                result.refreshToken(),
                result.sessionId(),
                result.tokenType(),
                result.accessTokenExpiresAt().getEpochSecond(),
                result.refreshTokenExpiresAt().getEpochSecond());
    }
}
