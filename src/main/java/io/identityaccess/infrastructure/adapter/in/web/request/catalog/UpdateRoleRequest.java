package io.identityaccess.infrastructure.adapter.in.web.request.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
        @NotBlank @Size(max = 255) String description) {}
