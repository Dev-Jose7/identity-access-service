package com.arka.identityaccess.application.command;

public record AssignRoleCommand(
        String userId,
        String roleCode,
        String actorUserId) {}
