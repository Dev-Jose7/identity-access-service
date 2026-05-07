package com.arka.identityaccess.domain.service;

import com.arka.identityaccess.domain.exception.InvalidCredentialsException;
import com.arka.identityaccess.domain.exception.UserNotEnabledException;
import com.arka.identityaccess.domain.model.user.entity.UserCredential;

public class PasswordPolicy {

    public void ensureCredentialUsable(UserCredential credential) {
        if (credential == null || !credential.isActive()) {
            throw new UserNotEnabledException();
        }
    }

    public void ensureRawPasswordProvided(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidCredentialsException();
        }
    }

    public void ensurePasswordMatches(boolean matches) {
        if (!matches) {
            throw new InvalidCredentialsException();
        }
    }
}
