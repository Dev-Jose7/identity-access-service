package io.identityaccess.infrastructure.adapter.in.web.response;

import java.time.Instant;
import java.util.Set;

public record AccountSummaryResponse(
        String userId,
        String email,
        String status,
        int failedLoginCount,
        Instant createdAt,
        Instant updatedAt,
        Set<String> roles) {}
