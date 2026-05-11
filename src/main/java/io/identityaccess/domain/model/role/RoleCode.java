package io.identityaccess.domain.model.role;

import io.identityaccess.domain.exception.RoleInvalidException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record RoleCode(String value) {

    private static final Pattern ROLE_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_:-]{1,99}$");

    public RoleCode {
        if (value == null || value.isBlank()) {
            throw new RoleInvalidException();
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!ROLE_CODE_PATTERN.matcher(value).matches()) {
            throw new RoleInvalidException();
        }
    }

    public static RoleCode of(String value) {
        return new RoleCode(value);
    }

    public static RoleCode from(String value) {
        return of(value);
    }

    @Override
    public String toString() {
        return value;
    }

    public boolean sameAs(String other) {
        return other != null && Objects.equals(value, RoleCode.of(other).value());
    }
}
