package com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper;

import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.entity.AccountCredential;
import com.arka.identityaccess.domain.identity.enumtype.AccountStatus;
import com.arka.identityaccess.domain.identity.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserCredentialRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserRow;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class UserRowMapper {

    public AccountAggregate toAggregate(UserRow userRow, UserCredentialRow credentialRow) {
        AccountStatus status = AccountStatus.valueOf(userRow.status().toUpperCase());
        int failedCount = userRow.failedLoginCount() == null ? 0 : Math.max(0, userRow.failedLoginCount());

        return AccountAggregate.rehydrate(
                AccountId.of(userRow.userId()),
                EmailAddress.of(userRow.email()),
                true,
                status,
                new AccountCredential(
                        credentialRow.credentialId(),
                        AccountId.of(credentialRow.userId()),
                        EmailAddress.of(userRow.email()),
                        credentialRow.passwordHash(),
                        CredentialStatus.valueOf(credentialRow.status().toUpperCase())),
                failedCount);
    }

    public UserRow toRow(AccountAggregate account) {
        Instant now = Instant.now();
        return new UserRow(
                account.id().value(),
                account.email().value(),
                account.status().name(),
                account.failedAuthenticationCount(),
                now,
                now);
    }
}
