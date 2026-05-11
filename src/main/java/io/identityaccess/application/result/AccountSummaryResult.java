package io.identityaccess.application.result;

import java.time.Instant;
import java.util.Set;

public record AccountSummaryResult(
        String userId,
        String email,
        String status,
        int failedLoginCount,
        Instant createdAt,
        Instant updatedAt,
        Set<String> roles) {}
