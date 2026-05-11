package io.identityaccess.infrastructure.adapter.in.web.response;

public record UnblockUserResponse(
        String userId,
        String status,
        boolean changed) {}
