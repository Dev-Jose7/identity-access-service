package io.identityaccess.application.command.catalog;

public record UpdatePermissionCommand(
        String permissionId,
        String resource,
        String action,
        String scope,
        String description,
        String actorUserId) {}
