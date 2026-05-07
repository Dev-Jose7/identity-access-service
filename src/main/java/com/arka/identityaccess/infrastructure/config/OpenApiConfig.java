package com.arka.identityaccess.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI identityAccessOpenApi() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
                .info(new Info()
                        .title("Identity Access Service API")
                        .description("Reactive API for authentication, session lifecycle and IAM administration in ArkaB2B")
                        .version("0.1.0-SNAPSHOT")
                        .contact(new Contact().name("ArkaB2B Platform Team"))
                        .license(new License().name("Proprietary")));
    }
}
