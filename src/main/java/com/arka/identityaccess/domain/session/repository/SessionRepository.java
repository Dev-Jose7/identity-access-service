package com.arka.identityaccess.domain.session.repository;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.valueobject.SessionId;
import java.util.List;
import java.util.Optional;

public interface SessionRepository {

    SessionAggregate save(SessionAggregate session);

    Optional<SessionAggregate> findById(SessionId sessionId);

    List<SessionAggregate> findActiveByAccountId(AccountId accountId);
}
