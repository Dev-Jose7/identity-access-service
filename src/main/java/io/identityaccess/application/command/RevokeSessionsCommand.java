package io.identityaccess.application.command;

public record RevokeSessionsCommand(
        String userId,
        String actorUserId,
        String reason) {}
