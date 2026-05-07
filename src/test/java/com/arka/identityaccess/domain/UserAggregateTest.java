package com.arka.identityaccess.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.identityaccess.domain.exception.InvalidCredentialsException;
import com.arka.identityaccess.domain.exception.UserNotEnabledException;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.entity.UserCredential;
import com.arka.identityaccess.domain.model.user.enumtype.CredentialStatus;
import com.arka.identityaccess.domain.model.user.enumtype.UserStatus;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import com.arka.identityaccess.domain.service.PasswordPolicy;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserAggregateTest {

    @Test
    void shouldAuthenticateWhenUserIsActiveAndPasswordMatches() {
        UserAggregate user = activeUser(UserStatus.ACTIVE);

        user.authenticate(true, new PasswordPolicy(), ClientIp.of("127.0.0.1"), Instant.now());

        assertEquals(1, user.loginAttempts().size());
        assertEquals(true, user.lastAttempt().success());
    }

    @Test
    void shouldRejectWhenUserIsBlocked() {
        UserAggregate user = activeUser(UserStatus.BLOCKED);

        assertThrows(UserNotEnabledException.class,
                () -> user.authenticate(true, new PasswordPolicy(), ClientIp.of("127.0.0.1"), Instant.now()));
    }

    @Test
    void shouldRejectWhenPasswordDoesNotMatch() {
        UserAggregate user = activeUser(UserStatus.ACTIVE);

        assertThrows(InvalidCredentialsException.class,
                () -> user.authenticate(false, new PasswordPolicy(), ClientIp.of("127.0.0.1"), Instant.now()));
    }

    private UserAggregate activeUser(UserStatus status) {
        UserId userId = UserId.of("usr-1");
        return new UserAggregate(
                userId,
                EmailAddress.of("user@arka.com"),
                status,
                new UserCredential("cred-1", userId, EmailAddress.of("user@arka.com"), "$2a$10$hash", CredentialStatus.ACTIVE),
                List.of(),
                Set.of("ORG_USER"));
    }
}
