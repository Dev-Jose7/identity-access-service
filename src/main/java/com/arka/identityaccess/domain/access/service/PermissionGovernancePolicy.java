package com.arka.identityaccess.domain.access.service;

import com.arka.identityaccess.domain.access.aggregate.PermissionAggregate;
import com.arka.identityaccess.domain.access.exception.PermissionInvalidException;

public class PermissionGovernancePolicy {

    public void ensureGrantable(PermissionAggregate permission) {
        if (permission == null || !permission.status().isGrantable()) {
            throw new PermissionInvalidException("permission is not grantable");
        }
    }
}
