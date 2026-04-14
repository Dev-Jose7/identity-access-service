package com.arka.identityaccess.domain.identity.valueobject;

import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record AuthenticationSecurityRules(
        int maxFailedAttemptsBeforeBlock,
        boolean resetFailuresOnSuccess,
        String lockoutReason,
        Set<String> lockoutFailureCodes) {

    public AuthenticationSecurityRules {
        if (maxFailedAttemptsBeforeBlock <= 0) {
            throw new DomainInvariantViolationException("max failed attempts before block must be positive");
        }
        if (lockoutReason == null || lockoutReason.isBlank()) {
            throw new DomainInvariantViolationException("lockout reason is required");
        }
        lockoutReason = lockoutReason.trim();
        lockoutFailureCodes = normalizeFailureCodes(lockoutFailureCodes);
    }

    public boolean shouldCountAgainstLockout(String failureCode) {
        if (failureCode == null || failureCode.isBlank()) {
            return false;
        }
        return lockoutFailureCodes.contains(failureCode.trim().toLowerCase(Locale.ROOT));
    }

    public boolean shouldBlockAfterFailedAttemptCount(int failedAttempts, AccountStatus currentStatus) {
        return currentStatus != null
                && currentStatus.canAuthenticate()
                && failedAttempts >= maxFailedAttemptsBeforeBlock;
    }

    private static Set<String> normalizeFailureCodes(Set<String> failureCodes) {
        if (failureCodes == null || failureCodes.isEmpty()) {
            return Set.of("invalid_credentials");
        }
        return failureCodes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }
}
