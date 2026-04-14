package com.arka.identityaccess.domain.identity.repository;

import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import java.util.Optional;

public interface AccountRepository {

    AccountAggregate save(AccountAggregate account);

    Optional<AccountAggregate> findById(AccountId accountId);

    Optional<AccountAggregate> findByEmail(EmailAddress emailAddress);

    boolean existsByEmail(EmailAddress emailAddress);
}
