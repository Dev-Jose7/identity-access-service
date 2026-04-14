package com.arka.identityaccess.domain.identity.service;

import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;

public class AccountActivationPolicy {

    private final boolean requireEmailVerification;

    public AccountActivationPolicy(boolean requireEmailVerification) {
        this.requireEmailVerification = requireEmailVerification;
    }

    public AccountStatus initialStatus() {
        return requireEmailVerification ? AccountStatus.PENDING_VERIFICATION : AccountStatus.ACTIVE;
    }

    public boolean requireEmailVerification() {
        return requireEmailVerification;
    }

    public void ensureCanActivate(AccountAggregate account) {
        if (account == null) {
            throw new DomainInvariantViolationException("account is required");
        }
        if (requireEmailVerification && !account.emailVerified()) {
            throw new DomainInvariantViolationException("account requires verified email before activation");
        }
    }
}
