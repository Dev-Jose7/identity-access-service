package com.arka.identityaccess.domain.shared.port;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import java.time.Instant;

public interface RevocationStore {

    void revokeSession(SessionId sessionId, String reason, Instant occurredAt);

    void revokeAllByAccount(AccountId accountId, String reason, Instant occurredAt);

    boolean isSessionRevoked(SessionId sessionId);
}
