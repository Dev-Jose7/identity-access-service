package com.arka.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank String sessionId) {}
