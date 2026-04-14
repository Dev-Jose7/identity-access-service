package com.arka.identityaccess.domain.shared.port;

import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;

public interface VerificationMessageSender {

    void sendAccountVerification(AccountId accountId, EmailAddress emailAddress, String verificationToken);
}
