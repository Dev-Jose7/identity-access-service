package com.arka.identityaccess.domain.model.role;

import com.arka.identityaccess.domain.exception.RoleInvalidException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum RoleCode {
    ORG_OWNER,
    ORG_ADMIN,
    ORG_MANAGER,
    ORG_USER,
    ORG_READONLY,
    ARKA_ADMIN;

    public static RoleCode from(String value) {
        if (value == null || value.isBlank()) {
            throw new RoleInvalidException();
        }
        try {
            return RoleCode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RoleInvalidException();
        }
    }

    public static Set<String> codes() {
        return Arrays.stream(values())
                .map(RoleCode::name)
                .collect(Collectors.toUnmodifiableSet());
    }
}
