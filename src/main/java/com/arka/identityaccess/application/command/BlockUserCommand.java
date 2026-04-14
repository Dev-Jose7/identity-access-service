package com.arka.identityaccess.application.command;

public record BlockUserCommand(
        String userId,
        String actorUserId,
        String reason) {}
