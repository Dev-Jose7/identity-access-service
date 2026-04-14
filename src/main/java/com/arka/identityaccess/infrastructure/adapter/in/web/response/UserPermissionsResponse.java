package com.arka.identityaccess.infrastructure.adapter.in.web.response;

import java.util.Set;

public record UserPermissionsResponse(
        String userId,
        Set<String> roles,
        Set<String> permissions) {}
