package io.identityaccess.application.mapper.result;

import io.identityaccess.application.result.LoginResult;
import io.identityaccess.domain.model.session.SessionAggregate;
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
