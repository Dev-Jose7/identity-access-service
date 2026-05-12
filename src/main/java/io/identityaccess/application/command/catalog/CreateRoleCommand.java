package io.identityaccess.application.command.catalog;

public record CreateRoleCommand(
        String roleCode,
        String description,
        boolean protectedRole,
        String actorUserId) {}
