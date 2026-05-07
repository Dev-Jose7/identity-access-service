package com.arka.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Size;

public record BlockUserRequest(@Size(max = 255) String reason) {}
