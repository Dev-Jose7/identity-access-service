package io.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshSessionRequest(
        @NotBlank @Size(min = 20, max = 4096) String refreshToken) {}
