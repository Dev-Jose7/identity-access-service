package io.identityaccess.application.command.catalog;

public record UpdateRoleCommand(
        String roleId,
        String description,
        String actorUserId) {}
