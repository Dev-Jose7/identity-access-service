package io.identityaccess.domain.model.session.valueobject;

public record ClientIp(String value) {

    public ClientIp {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ipAddress is required");
        }
        value = value.trim();
    }

    public static ClientIp of(String value) {
        return new ClientIp(value);
    }
}
