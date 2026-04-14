package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response;

import com.arka.identityaccess.application.result.IntrospectResult;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.IntrospectResponse;
import org.springframework.stereotype.Component;

@Component
public class IntrospectResponseMapper {

    public IntrospectResponse toResponse(IntrospectResult result) {
        return new IntrospectResponse(
                result.active(),
                result.inactiveReason(),
                result.subject(),
                result.sessionId(),
                result.tokenType(),
                result.issuer(),
                result.audience(),
                result.issuedAtEpochSecond(),
                result.expiresAtEpochSecond(),
                result.jti(),
                result.email(),
                result.roles(),
                result.permissions());
    }
}
