package com.arka.identityaccess.application.mapper.result;

import com.arka.identityaccess.application.result.TokenPairResult;
import com.arka.identityaccess.domain.model.session.SessionAggregate;
import org.springframework.stereotype.Component;

@Component
public class TokenPairResultMapper {
    public TokenPairResult toResult(SessionAggregate session, String accessToken, String refreshToken) {
        return new TokenPairResult(accessToken, refreshToken, session.id().value(), "Bearer", session.timestamps().accessTokenExpiresAt(), session.timestamps().refreshTokenExpiresAt());
    }
}
