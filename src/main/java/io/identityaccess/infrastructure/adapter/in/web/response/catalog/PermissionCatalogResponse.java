package io.identityaccess.infrastructure.adapter.in.web.response.catalog;

public record PermissionCatalogResponse(
        String permissionId,
        String permissionCode,
        String resource,
        String action,
        String scope,
        String description,
        String status,
        boolean systemPermission) {}
