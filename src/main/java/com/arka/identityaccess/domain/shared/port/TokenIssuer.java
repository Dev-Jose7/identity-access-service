package com.arka.identityaccess.domain.shared.port;

import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;

public interface TokenIssuer {

    String issueAccessToken(SessionAggregate session, AccessProfile accessProfile);

    String issueRefreshToken(SessionAggregate session);
}
