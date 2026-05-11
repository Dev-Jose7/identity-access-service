package io.identityaccess.domain.service;

import io.identityaccess.domain.exception.InvalidCredentialsException;
import io.identityaccess.domain.exception.UserNotEnabledException;
import io.identityaccess.domain.model.user.entity.UserCredential;

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
