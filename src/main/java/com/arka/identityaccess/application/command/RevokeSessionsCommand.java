package com.arka.identityaccess.application.command;

public record RevokeSessionsCommand(
        String userId,
        String actorUserId,
        String reason) {}
