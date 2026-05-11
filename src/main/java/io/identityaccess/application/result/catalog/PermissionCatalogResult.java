package io.identityaccess.application.result.catalog;

public record PermissionCatalogResult(
        String permissionId,
        String permissionCode,
        String resource,
        String action,
        String scope,
        String description,
        String status,
        boolean systemPermission) {}
