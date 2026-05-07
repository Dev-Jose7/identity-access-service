package com.arka.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Size;

public record RevokeSessionsRequest(@Size(max = 255) String reason) {}
