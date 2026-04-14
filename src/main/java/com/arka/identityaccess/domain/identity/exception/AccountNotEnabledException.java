package com.arka.identityaccess.domain.identity.exception;

import com.arka.identityaccess.domain.shared.exception.DomainException;

public class AccountNotEnabledException extends DomainException {

    public AccountNotEnabledException() {
        super("cuenta_no_habilitada", "Account is not enabled for authentication");
    }
}
