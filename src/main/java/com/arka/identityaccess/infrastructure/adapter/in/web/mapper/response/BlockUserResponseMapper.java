package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response;

import com.arka.identityaccess.application.result.BlockUserResult;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.BlockUserResponse;
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
