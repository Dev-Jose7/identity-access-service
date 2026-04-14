package com.arka.identityaccess.infrastructure;

import com.arka.identityaccess.infrastructure.adapter.in.web.controller.JwksHttpController;
import com.arka.identityaccess.infrastructure.adapter.out.security.JwtRsaKeyProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(
        controllers = JwksHttpController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
@Import(JwtRsaKeyProvider.class)
class JwksHttpControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldExposeRsaJwksWithConfiguredKid() {
        webTestClient.get()
                .uri("/.well-known/jwks.json")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.keys[0].kty").isEqualTo("RSA")
                .jsonPath("$.keys[0].alg").isEqualTo("RS256")
                .jsonPath("$.keys[0].use").isEqualTo("sig")
                .jsonPath("$.keys[0].kid").isEqualTo("dev-rsa-key-1")
                .jsonPath("$.keys[0].n").isNotEmpty()
                .jsonPath("$.keys[0].e").isNotEmpty();
    }
}
