package com.arka.identityaccess.domain.access.repository;

import com.arka.identityaccess.domain.access.aggregate.AccountAccessAggregate;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.util.Optional;

public interface AccountAccessRepository {

    AccountAccessAggregate save(AccountAccessAggregate accountAccess);

    Optional<AccountAccessAggregate> findByAccountId(AccountId accountId);
}
