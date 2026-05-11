package io.identityaccess.infrastructure.adapter.in.web.mapper.response;

import io.identityaccess.application.result.TokenPairResult;
import io.identityaccess.infrastructure.adapter.in.web.response.TokenPairResponse;
import org.springframework.stereotype.Component;

@Component
public class TokenPairResponseMapper {
    public TokenPairResponse toResponse(TokenPairResult result) {
        return new TokenPairResponse(result.accessToken(), result.refreshToken(), result.sessionId(), result.tokenType(), result.accessTokenExpiresAt(), result.refreshTokenExpiresAt());
    }
}
