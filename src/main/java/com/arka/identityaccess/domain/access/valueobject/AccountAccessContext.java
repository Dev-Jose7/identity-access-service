package com.arka.identityaccess.domain.access.valueobject;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;

public record AccountAccessContext(
        AccountId accountId,
        boolean operable) {

    public AccountAccessContext {
        if (accountId == null) {
            throw new DomainInvariantViolationException("account access context requires accountId");
        }
    }

    public static AccountAccessContext of(AccountId accountId, boolean operable) {
        return new AccountAccessContext(accountId, operable);
    }
}
