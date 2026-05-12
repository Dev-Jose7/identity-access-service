package io.identityaccess.application.result;

public record BlockUserResult(
        String userId,
        String status,
        boolean changed) {}
