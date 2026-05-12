package io.identityaccess.application.command;

public record BlockUserCommand(
        String userId,
        String actorUserId,
        String reason) {}
