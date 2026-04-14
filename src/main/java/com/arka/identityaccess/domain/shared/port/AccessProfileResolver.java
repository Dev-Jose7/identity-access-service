package com.arka.identityaccess.domain.shared.port;

import com.arka.identityaccess.domain.access.valueobject.AccessProfile;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;

public interface AccessProfileResolver {

    AccessProfile resolve(AccountId accountId);
}
