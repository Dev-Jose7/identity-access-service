package com.arka.identityaccess.application.result;

import java.util.Set;

public record UserPermissionsResult(
        String userId,
        Set<String> roles,
        Set<String> permissions) {}
