package com.arka.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AssignRoleRequest(
        @NotBlank
        @Pattern(
                regexp = "^(ORG_OWNER|ORG_ADMIN|ORG_MANAGER|ORG_USER|ORG_READONLY|ARKA_ADMIN)$",
                message = "roleCode is invalid")
        String roleCode) {}
