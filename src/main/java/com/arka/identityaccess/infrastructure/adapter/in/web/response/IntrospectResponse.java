package com.arka.identityaccess.infrastructure.adapter.in.web.response;

import java.util.List;
import java.util.Set;

public record IntrospectResponse(
        boolean active,
        String inactiveReason,
        String sub,
        String sid,
        String typ,
        String iss,
        List<String> aud,
        Long iat,
        Long exp,
        String jti,
        String email,
        Set<String> roles,
        Set<String> permissions) {}
