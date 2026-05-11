package io.identityaccess.application.command;

public record UnblockUserCommand(
        String userId,
        String actorUserId,
        String reason) {}
