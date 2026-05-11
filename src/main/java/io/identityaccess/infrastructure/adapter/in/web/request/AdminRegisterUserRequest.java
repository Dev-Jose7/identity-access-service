package io.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminRegisterUserRequest(
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @Pattern(
                regexp = "^[A-Za-z][A-Za-z0-9_:-]{1,99}$",
                message = "roleCode must be a valid role code")
        String roleCode) {}
