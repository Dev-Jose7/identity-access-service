package com.arka.identityaccess.domain.session.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.arka.identityaccess.domain.session.exception.SessionRefreshNotAllowedException;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.enumtype.SessionStatus;
import com.arka.identityaccess.domain.session.valueobject.AccessJti;
import com.arka.identityaccess.domain.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.valueobject.RefreshJti;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import com.arka.identityaccess.domain.session.valueobject.SessionTimestamps;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SessionPolicyTest {

    @Test
    void shouldRejectRefreshForRevokedSession() {
        SessionPolicy sessionPolicy = new SessionPolicy();
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        SessionAggregate session = SessionAggregate.rehydrate(
                SessionId.of("11111111-1111-1111-1111-111111111111"),
                AccountId.of("usr-1"),
                ClientDevice.of("dev-1", "MacBook", "desktop"),
                ClientIp.of("10.0.0.1"),
                AccessJti.of("22222222-2222-2222-2222-222222222222"),
                RefreshJti.of("33333333-3333-3333-3333-333333333333"),
                SessionTimestamps.of(now.minusSeconds(60), now.plusSeconds(900), now.plusSeconds(3600)),
                SessionStatus.REVOKED);

        assertThrows(
                SessionRefreshNotAllowedException.class,
                () -> sessionPolicy.ensureSessionCanRefresh(session, now));
    }

}
