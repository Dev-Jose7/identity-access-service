package com.arka.identityaccess.application.exception;

public class AccountNotFoundException extends ApplicationException {

    public AccountNotFoundException() {
        super("cuenta_no_encontrada", "Account does not exist");
    }
}
