package io.identityaccess.infrastructure.adapter.in.web.mapper.response;

import io.identityaccess.application.result.UnblockUserResult;
import io.identityaccess.infrastructure.adapter.in.web.response.UnblockUserResponse;
import org.springframework.stereotype.Component;

@Component
public class UnblockUserResponseMapper {

    public UnblockUserResponse toResponse(UnblockUserResult result) {
        return new UnblockUserResponse(
                result.userId(),
                result.status(),
                result.changed());
    }
}
