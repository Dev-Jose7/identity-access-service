package io.identityaccess.application.result.catalog;

public record PermissionGrantResult(
        String roleId,
        String permissionCode,
        boolean changed,
        String status) {}
