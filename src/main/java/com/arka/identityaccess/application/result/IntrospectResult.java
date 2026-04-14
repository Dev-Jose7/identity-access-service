package com.arka.identityaccess.application.result;

import java.util.List;
import java.util.Set;

public record IntrospectResult(
        boolean active,
        String inactiveReason,
        String subject,
        String sessionId,
        String tokenType,
        String issuer,
        List<String> audience,
        Long issuedAtEpochSecond,
        Long expiresAtEpochSecond,
        String jti,
        String email,
        Set<String> roles,
        Set<String> permissions) {}
