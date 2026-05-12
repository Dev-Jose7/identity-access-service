package io.identityaccess.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Size;

public record UnblockUserRequest(@Size(max = 255) String reason) {}
