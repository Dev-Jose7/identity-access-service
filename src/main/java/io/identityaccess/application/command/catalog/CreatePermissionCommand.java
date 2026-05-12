package io.identityaccess.application.command.catalog;

public record CreatePermissionCommand(
        String permissionCode,
        String resource,
        String action,
        String scope,
        String description,
        boolean systemPermission,
        String actorUserId) {}
