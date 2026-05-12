package io.identityaccess.infrastructure.adapter.in.web.request.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record GrantPermissionToRoleRequest(
        @NotBlank
        @Pattern(regexp = "^[a-z][a-z0-9.-]{1,149}$", message = "permissionCode must be a valid permission code")
        String permissionCode) {}
