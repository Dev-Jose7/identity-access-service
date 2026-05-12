package io.identityaccess.application.result;

public record UnblockUserResult(
        String userId,
        String status,
        boolean changed) {}
