package io.identityaccess.infrastructure.adapter.in.web.request.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePermissionRequest(
        @NotBlank
        @Pattern(regexp = "^[a-z][a-z0-9.-]{1,149}$", message = "permissionCode must be a valid permission code")
        String permissionCode,
        @NotBlank
        @Pattern(regexp = "^[a-z][a-z0-9_.:-]{1,99}$", message = "resource must be a valid resource")
        String resource,
        @NotBlank
        @Pattern(regexp = "^[a-z][a-z0-9_.:-]{1,99}$", message = "action must be a valid action")
        String action,
        @Pattern(regexp = "^$|^[A-Za-z][A-Za-z0-9_:-]{1,49}$", message = "scope must be a valid scope")
        String scope,
        @NotBlank @Size(max = 255) String description,
        boolean systemPermission) {}
