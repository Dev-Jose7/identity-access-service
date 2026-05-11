package io.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

    public record AssignRoleRequest(
            @NotBlank
            @Pattern(
                    regexp = "^[A-Za-z][A-Za-z0-9_:-]{1,99}$",
                    message = "roleCode is invalid")
            String roleCode) {}
