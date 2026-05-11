package io.identityaccess.application.command;

public record AssignRoleCommand(
        String userId,
        String roleCode,
        String actorUserId) {}
