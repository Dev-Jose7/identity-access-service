package com.arka.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminRegisterUserRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @Pattern(
                regexp = "^(ORG_ADMIN|ORG_MANAGER|ORG_USER|ORG_READONLY)$",
                message = "roleCode must be one of ORG_ADMIN, ORG_MANAGER, ORG_USER or ORG_READONLY")
        String roleCode) {}
