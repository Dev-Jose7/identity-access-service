package com.arka.identityaccess.application.mapper.result;

import com.arka.identityaccess.application.result.LoginResult;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import org.springframework.stereotype.Component;

@Component
public class LoginResultMapper {

    public LoginResult toResult(SessionAggregate session, String accessToken, String refreshToken) {
        return new LoginResult(
                accessToken,
                refreshToken,
                session.id().value(),
                "Bearer",
                session.timestamps().accessTokenExpiresAt(),
                session.timestamps().refreshTokenExpiresAt());
    }
}
