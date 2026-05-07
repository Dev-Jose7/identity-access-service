package com.arka.identityaccess.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.arka.identityaccess.application.port.in.AssignRoleCommandUseCase;
import com.arka.identityaccess.application.port.in.BlockUserCommandUseCase;
import com.arka.identityaccess.application.port.in.GetUserPermissionsQueryUseCase;
import com.arka.identityaccess.application.port.in.IntrospectTokenQueryUseCase;
import com.arka.identityaccess.application.port.in.LoginCommandUseCase;
import com.arka.identityaccess.application.port.in.LogoutCommandUseCase;
import com.arka.identityaccess.application.port.in.RefreshSessionCommandUseCase;
import com.arka.identityaccess.application.port.in.RegisterCommandUseCase;
import com.arka.identityaccess.application.port.in.RevokeSessionsCommandUseCase;
import com.arka.identityaccess.application.result.IntrospectResult;
import com.arka.identityaccess.application.result.LoginResult;
import com.arka.identityaccess.application.result.UserPermissionsResult;
import com.arka.identityaccess.domain.exception.InvalidCredentialsException;
import com.arka.identityaccess.domain.exception.RateLimitExceededException;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.AdminIamHttpController;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.AuthHttpController;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.SessionAdminHttpController;
import com.arka.identityaccess.infrastructure.adapter.in.web.controller.TokenIntrospectController;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.AssignRoleCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.BlockUserCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.GetUserPermissionsQueryMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.LoginCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.LogoutCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.RefreshSessionCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.RegisterCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.RevokeSessionsCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.query.IntrospectQueryMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.AssignRoleResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.BlockUserResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.IntrospectResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.LoginResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.LogoutResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.RegisterResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.RevokeSessionsResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.TokenPairResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.UserPermissionsResponseMapper;
import com.arka.identityaccess.infrastructure.config.WebExceptionHandlerConfig;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(
        controllers = {
                AuthHttpController.class,
                TokenIntrospectController.class,
                AdminIamHttpController.class,
                SessionAdminHttpController.class
        },
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
@Import({
        LoginCommandMapper.class,
        RegisterCommandMapper.class,
        RefreshSessionCommandMapper.class,
        LogoutCommandMapper.class,
        AssignRoleCommandMapper.class,
        BlockUserCommandMapper.class,
        RevokeSessionsCommandMapper.class,
        GetUserPermissionsQueryMapper.class,
        IntrospectQueryMapper.class,
        LoginResponseMapper.class,
        RegisterResponseMapper.class,
        TokenPairResponseMapper.class,
        LogoutResponseMapper.class,
        AssignRoleResponseMapper.class,
        BlockUserResponseMapper.class,
        RevokeSessionsResponseMapper.class,
        UserPermissionsResponseMapper.class,
        IntrospectResponseMapper.class,
        WebExceptionHandlerConfig.class
})
class AuthHttpControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private LoginCommandUseCase loginCommandUseCase;

    @MockBean
    private RegisterCommandUseCase registerCommandUseCase;

    @MockBean
    private RefreshSessionCommandUseCase refreshSessionCommandUseCase;

    @MockBean
    private LogoutCommandUseCase logoutCommandUseCase;

    @MockBean
    private AssignRoleCommandUseCase assignRoleCommandUseCase;

    @MockBean
    private BlockUserCommandUseCase blockUserCommandUseCase;

    @MockBean
    private RevokeSessionsCommandUseCase revokeSessionsCommandUseCase;

    @MockBean
    private GetUserPermissionsQueryUseCase getUserPermissionsQueryUseCase;

    @MockBean
    private IntrospectTokenQueryUseCase introspectTokenQueryUseCase;

    @Test
    void shouldReturnLoginResponseWhenCredentialsAreValid() {
        when(loginCommandUseCase.handle(any()))
                .thenReturn(Mono.just(new LoginResult(
                        "access-token",
                        "refresh-token",
                        "session-1",
                        "Bearer",
                        Instant.parse("2026-01-01T00:15:00Z"),
                        Instant.parse("2026-01-08T00:00:00Z"))));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "JUnit")
                .bodyValue("""
                        {
                          \"email\": \"user@arka.com\",
                          \"password\": \"secret123\"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token")
                .jsonPath("$.refreshToken").isEqualTo("refresh-token")
                .jsonPath("$.tokenType").isEqualTo("Bearer");
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() {
        when(loginCommandUseCase.handle(any())).thenReturn(Mono.error(new InvalidCredentialsException()));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "JUnit")
                .bodyValue("""
                        {
                          \"email\": \"user@arka.com\",
                          \"password\": \"wrong123\"
                        }
                        """)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("credenciales_invalidas");
    }

    @Test
    void shouldReturnTooManyRequestsWhenLoginRateLimitIsExceeded() {
        when(loginCommandUseCase.handle(any()))
                .thenReturn(Mono.error(new RateLimitExceededException("Login rate limit exceeded")));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("User-Agent", "JUnit")
                .bodyValue("""
                        {
                          \"email\": \"user@arka.com\",
                          \"password\": \"secret123\"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectBody()
                .jsonPath("$.code").isEqualTo("operacion_no_permitida");
    }

    @Test
    void shouldReturnTooManyRequestsWhenRefreshRateLimitIsExceeded() {
        when(refreshSessionCommandUseCase.handle(any()))
                .thenReturn(Mono.error(new RateLimitExceededException("Refresh rate limit exceeded")));

        webTestClient.post()
                .uri("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          \"refreshToken\": \"eyJhbGciOiJSUzI1NiJ9.eyJ0eXAiOiJyZWZyZXNoIn0.signature\"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectBody()
                .jsonPath("$.code").isEqualTo("operacion_no_permitida");
    }

    @Test
    void shouldReturnTooManyRequestsWhenFounderRegisterRateLimitIsExceeded() {
        when(registerCommandUseCase.handle(any()))
                .thenReturn(Mono.error(new RateLimitExceededException("Register-founder rate limit exceeded")));

        webTestClient.post()
                .uri("/api/v1/auth/register-founder")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          \"email\": \"founder@arka.com\",
                          \"password\": \"PlainSecret123\"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectBody()
                .jsonPath("$.code").isEqualTo("operacion_no_permitida");
    }

    @Test
    void shouldReturnIntrospectResponse() {
        when(introspectTokenQueryUseCase.handle(any()))
                .thenReturn(Mono.just(new IntrospectResult(
                        true,
                        null,
                        "usr-1",
                        "ses-1",
                        "access",
                        "identity-access-service",
                        List.of("arka-b2b"),
                        1735689600L,
                        1735690500L,
                        "acc-jti-1",
                        "user@arka.com",
                        Set.of("ORG_OWNER"),
                        Set.of("iam.user.read"))));

        webTestClient.post()
                .uri("/api/v1/auth/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          \"token\": \"Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJ1c3ItMSJ9.signature\"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.active").isEqualTo(true)
                .jsonPath("$.sub").isEqualTo("usr-1")
                .jsonPath("$.sid").isEqualTo("ses-1")
                .jsonPath("$.email").isEqualTo("user@arka.com")
                .jsonPath("$.roles[0]").isEqualTo("ORG_OWNER")
                .jsonPath("$.permissions[0]").isEqualTo("iam.user.read");
    }

    @Test
    void shouldValidateIntrospectRequestBody() {
        webTestClient.post()
                .uri("/api/v1/auth/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          \"token\": \"short\"
                        }
                        """)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.code").isEqualTo("IamDependencyUnavailableException");
    }

    @Test
    void shouldReturnUserPermissionsResponse() {
        when(getUserPermissionsQueryUseCase.handle(any()))
                .thenReturn(Mono.just(new UserPermissionsResult(
                        "usr-5",
                        Set.of("ORG_ADMIN"),
                        Set.of("iam.user.read", "iam.user.create"))));

        webTestClient.get()
                .uri("/api/v1/admin/iam/users/usr-5/permissions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo("usr-5")
                .jsonPath("$.roles[0]").isEqualTo("ORG_ADMIN")
                .jsonPath("$.permissions[0]").isNotEmpty();
    }
}
