package io.identityaccess.infrastructure.adapter.in.security;

import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;

public class JwtAuthenticationTokenRequest extends AbstractAuthenticationToken {

    private final String token;

    public JwtAuthenticationTokenRequest(String token) {
        super(List.of());
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token is required");
        }
        this.token = token;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return token;
    }

    public String token() {
        return token;
    }
}
