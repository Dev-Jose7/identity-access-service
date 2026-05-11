package io.identityaccess.infrastructure.adapter.in.web.request.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_:-]{1,99}$", message = "roleCode must be a valid role code")
        String roleCode,
        @NotBlank @Size(max = 255) String description,
        boolean protectedRole) {}
