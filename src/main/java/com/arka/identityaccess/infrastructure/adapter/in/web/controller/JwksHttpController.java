package com.arka.identityaccess.infrastructure.adapter.in.web.controller;

import com.arka.identityaccess.infrastructure.adapter.out.security.JwtRsaKeyProvider;
import com.nimbusds.jose.jwk.JWKSet;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwksHttpController {

    private final JwtRsaKeyProvider jwtRsaKeyProvider;

    public JwksHttpController(JwtRsaKeyProvider jwtRsaKeyProvider) {
        this.jwtRsaKeyProvider = jwtRsaKeyProvider;
    }

    @GetMapping("${app.security.jwt.jwks-path:/.well-known/jwks.json}")
    public Map<String, Object> jwks() {
        return new JWKSet(jwtRsaKeyProvider.publicJwks()).toJSONObject();
    }
}
