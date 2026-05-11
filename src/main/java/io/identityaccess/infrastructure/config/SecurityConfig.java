package io.identityaccess.infrastructure.config;

import io.identityaccess.infrastructure.adapter.in.security.BearerTokenServerAuthenticationConverter;
import io.identityaccess.infrastructure.adapter.in.security.JsonAccessDeniedHandler;
import io.identityaccess.infrastructure.adapter.in.security.JsonAuthenticationEntryPoint;
import io.identityaccess.infrastructure.adapter.in.security.JwtReactiveAuthenticationManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    private final String jwksPath;

    public SecurityConfig(@Value("${app.security.jwt.jwks-path:/.well-known/jwks.json}") String jwksPath) {
        this.jwksPath = jwksPath;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            JwtReactiveAuthenticationManager authenticationManager,
            BearerTokenServerAuthenticationConverter bearerTokenConverter,
            JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler) {

        AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(authenticationManager);
        bearerAuthenticationFilter.setServerAuthenticationConverter(bearerTokenConverter);
        bearerAuthenticationFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.anyExchange());

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(spec -> spec
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST,
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/register",
                                "/api/v1/auth/register-primary",
                                "/api/v1/auth/introspect")
                        .permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/admin/iam/accounts")
                        .authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/logout")
                        .authenticated()
                        .pathMatchers(
                                HttpMethod.GET,
                                "/actuator/health",
                                "/actuator/info",
                                "/",
                                "/index.html",
                                "/console",
                                "/console/",
                                "/console/**",
                                jwksPath,
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**")
                        .permitAll()
                        .anyExchange().authenticated())
                .addFilterAt(bearerAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
