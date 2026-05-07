package com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper;

import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.entity.UserCredential;
import com.arka.identityaccess.domain.model.user.entity.UserLoginAttempt;
import com.arka.identityaccess.domain.model.user.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.model.user.enumtype.UserStatus;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserCredentialRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserLoginAttemptRow;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserRow;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class UserRowMapper {

    public UserAggregate toAggregate(
            UserRow userRow,
            UserCredentialRow credentialRow,
            List<UserLoginAttemptRow> attemptRows,
            List<String> roleCodes) {
        List<UserLoginAttempt> attempts = attemptRows.stream()
                .map(row -> new UserLoginAttempt(row.attemptId(), UserId.of(row.userId()), row.attemptAt(), com.arka.identityaccess.domain.model.session.valueobject.ClientIp.of(row.ipAddress()), row.success()))
                .toList();
        return new UserAggregate(
                UserId.of(userRow.userId()),
                EmailAddress.of(userRow.email()),
                UserStatus.valueOf(userRow.status().toUpperCase()),
                new UserCredential(
                        credentialRow.credentialId(),
                        UserId.of(credentialRow.userId()),
                        EmailAddress.of(userRow.email()),
                        credentialRow.passwordHash(),
                        CredentialStatus.valueOf(credentialRow.status().toUpperCase())),
                attempts,
                Set.copyOf(roleCodes));
    }

    public UserRow toRow(UserAggregate user) {
        Instant now = Instant.now();
        return new UserRow(
                user.id().value(),
                user.email().value(),
                user.status().name(),
                user.loginAttempts().size(),
                now,
                now);
    }
}
