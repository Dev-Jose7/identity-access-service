package com.arka.identityaccess.domain.identity.service;

import com.arka.identityaccess.domain.identity.valueobject.AuthenticationSecurityRules;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.Set;

public class AuthenticationSecurityPolicy {

    private final int maxFailedAttemptsBeforeBlock;
    private final boolean resetFailuresOnSuccess;
    private final String lockoutReason;
    private final Set<String> lockoutFailureCodes;

    public AuthenticationSecurityPolicy(
            int maxFailedAttemptsBeforeBlock,
            boolean resetFailuresOnSuccess,
            String lockoutReason) {
        if (maxFailedAttemptsBeforeBlock <= 0) {
            throw new DomainInvariantViolationException("max failed attempts before block must be positive");
        }
        if (lockoutReason == null || lockoutReason.isBlank()) {
            throw new DomainInvariantViolationException("lockout reason is required");
        }
        this.maxFailedAttemptsBeforeBlock = maxFailedAttemptsBeforeBlock;
        this.resetFailuresOnSuccess = resetFailuresOnSuccess;
        this.lockoutReason = lockoutReason.trim();
        this.lockoutFailureCodes = Set.of("invalid_credentials");
    }

    public AuthenticationSecurityPolicy(int maxFailedAttemptsBeforeBlock) {
        this(maxFailedAttemptsBeforeBlock, true, "FAILED_AUTHENTICATION_THRESHOLD");
    }

    public int maxFailedAttemptsBeforeBlock() {
        return maxFailedAttemptsBeforeBlock;
    }

    public AuthenticationSecurityRules rules() {
        return new AuthenticationSecurityRules(
                maxFailedAttemptsBeforeBlock,
                resetFailuresOnSuccess,
                lockoutReason,
                lockoutFailureCodes);
    }
}
