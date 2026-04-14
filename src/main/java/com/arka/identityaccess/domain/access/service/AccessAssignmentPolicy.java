package com.arka.identityaccess.domain.access.service;

import com.arka.identityaccess.domain.access.exception.AccessAssignmentException;
import com.arka.identityaccess.domain.access.valueobject.AccountAccessContext;
import com.arka.identityaccess.domain.access.valueobject.RoleAssignmentContext;

public class AccessAssignmentPolicy {

    public void ensureAssignable(AccountAccessContext accountContext, RoleAssignmentContext roleContext) {
        if (accountContext == null) {
            throw new AccessAssignmentException("target account is required");
        }
        if (roleContext == null) {
            throw new AccessAssignmentException("target role is required");
        }
        if (!accountContext.operable()) {
            throw new AccessAssignmentException("target account is not operable");
        }
        if (!roleContext.assignable()) {
            throw new AccessAssignmentException("target role is not assignable");
        }
        if (roleContext.protectedRole()) {
            throw new AccessAssignmentException("target role is protected and cannot be assigned");
        }
    }
}
