package com.arka.identityaccess.application.exception;

public class AccountAlreadyExistsException extends ApplicationException {

    public AccountAlreadyExistsException() {
        super("AccountAlreadyExistsException", "Account already exists");
    }
}
