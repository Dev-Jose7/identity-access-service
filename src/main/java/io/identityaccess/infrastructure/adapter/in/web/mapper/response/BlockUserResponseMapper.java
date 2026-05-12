package io.identityaccess.infrastructure.adapter.in.web.mapper.response;

import io.identityaccess.application.result.BlockUserResult;
import io.identityaccess.infrastructure.adapter.in.web.response.BlockUserResponse;
import org.springframework.stereotype.Component;

@Component
public class BlockUserResponseMapper {

    public BlockUserResponse toResponse(BlockUserResult result) {
        return new BlockUserResponse(
                result.userId(),
                result.status(),
                result.changed());
    }
}
