package com.arka.identityaccess.domain.identity.valueobject;

import com.arka.identityaccess.domain.shared.exception.DomainInvariantViolationException;
import java.util.UUID;
import java.util.regex.Pattern;

public record AccountId(String value) {

    private static final Pattern ACCOUNT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9-]{2,127}$");

    public AccountId {
        if (value == null || value.isBlank()) {
            throw new DomainInvariantViolationException("accountId is required");
        }
        value = value.trim();
        if (!ACCOUNT_ID_PATTERN.matcher(value).matches()) {
            throw new DomainInvariantViolationException("accountId format is invalid");
        }
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID().toString());
    }

    public static AccountId of(String value) {
        return new AccountId(value);
    }
}
