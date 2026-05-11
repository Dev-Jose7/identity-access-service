package io.identityaccess.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.identityaccess.application.command.RegisterCommand;
import io.identityaccess.application.port.in.AssignRoleCommandUseCase;
import io.identityaccess.application.port.in.AccessCatalogCommandUseCase;
import io.identityaccess.application.port.in.BlockUserCommandUseCase;
import io.identityaccess.application.port.in.GetUserPermissionsQueryUseCase;
import io.identityaccess.application.port.in.IntrospectTokenQueryUseCase;
import io.identityaccess.application.port.in.ListAccountsQueryUseCase;
import io.identityaccess.application.port.in.ListSessionsQueryUseCase;
import io.identityaccess.application.port.in.LoginCommandUseCase;
import io.identityaccess.application.port.in.LogoutCommandUseCase;
import io.identityaccess.application.port.in.RefreshSessionCommandUseCase;
import io.identityaccess.application.port.in.RegisterCommandUseCase;
import io.identityaccess.application.port.in.RevokeSessionsCommandUseCase;
import io.identityaccess.application.port.in.UnblockUserCommandUseCase;
import io.identityaccess.application.result.AccountSummaryResult;
import io.identityaccess.application.result.IntrospectResult;
import io.identityaccess.application.result.LoginResult;
import io.identityaccess.application.result.RegisterResult;
import io.identityaccess.application.result.SessionSummaryResult;
import io.identityaccess.application.result.UserPermissionsResult;
import io.identityaccess.application.result.catalog.PermissionCatalogResult;
import io.identityaccess.application.result.catalog.RoleCatalogResult;
import io.identityaccess.domain.exception.InvalidCredentialsException;
import io.identityaccess.domain.exception.PrimaryAccountAlreadyExistsException;
import io.identityaccess.domain.exception.RateLimitExceededException;
import io.identityaccess.infrastructure.adapter.in.web.controller.AdminIamHttpController;
import io.identityaccess.infrastructure.adapter.in.web.controller.AccessCatalogHttpController;
import io.identityaccess.infrastructure.adapter.in.web.controller.AuthHttpController;
import io.identityaccess.infrastructure.adapter.in.web.controller.SessionAdminHttpController;
import io.identityaccess.infrastructure.adapter.in.web.controller.TokenIntrospectController;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.AssignRoleCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.BlockUserCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.GetUserPermissionsQueryMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.LoginCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.LogoutCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.RefreshSessionCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.RegisterCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.RevokeSessionsCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.UnblockUserCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.query.IntrospectQueryMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.AssignRoleResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.BlockUserResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.AccountSummaryResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.IntrospectResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.LoginResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.LogoutResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.RegisterResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.RevokeSessionsResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.SessionSummaryResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.TokenPairResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.UnblockUserResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.UserPermissionsResponseMapper;
import io.identityaccess.infrastructure.config.WebExceptionHandlerConfig;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(
        controllers = {
                AuthHttpController.class,
                TokenIntrospectController.class,
                AdminIamHttpController.class,
                SessionAdminHttpController.class,
                AccessCatalogHttpController.class
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
        UnblockUserCommandMapper.class,
        RevokeSessionsCommandMapper.class,
        GetUserPermissionsQueryMapper.class,
        IntrospectQueryMapper.class,
        LoginResponseMapper.class,
        RegisterResponseMapper.class,
        AccountSummaryResponseMapper.class,
        TokenPairResponseMapper.class,
        LogoutResponseMapper.class,
        AssignRoleResponseMapper.class,
        BlockUserResponseMapper.class,
        UnblockUserResponseMapper.class,
        RevokeSessionsResponseMapper.class,
        SessionSummaryResponseMapper.class,
        UserPermissionsResponseMapper.class,
        IntrospectResponseMapper.class,
        WebExceptionHandlerConfig.class
})
class AuthHttpControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private LoginCommandUseCase loginCommandUseCase;

    @MockitoBean
    private RegisterCommandUseCase registerCommandUseCase;

    @MockitoBean
    private ListAccountsQueryUseCase listAccountsQueryUseCase;

    @MockitoBean
    private RefreshSessionCommandUseCase refreshSessionCommandUseCase;

    @MockitoBean
    private LogoutCommandUseCase logoutCommandUseCase;

    @MockitoBean
    private AssignRoleCommandUseCase assignRoleCommandUseCase;

    @MockitoBean
    private BlockUserCommandUseCase blockUserCommandUseCase;

    @MockitoBean
    private UnblockUserCommandUseCase unblockUserCommandUseCase;

    @MockitoBean
    private RevokeSessionsCommandUseCase revokeSessionsCommandUseCase;

    @MockitoBean
    private ListSessionsQueryUseCase listSessionsQueryUseCase;

    @MockitoBean
    private GetUserPermissionsQueryUseCase getUserPermissionsQueryUseCase;

    @MockitoBean
    private IntrospectTokenQueryUseCase introspectTokenQueryUseCase;

    @MockitoBean
    private AccessCatalogCommandUseCase accessCatalogCommandUseCase;

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
                          \"email\": \"user@example.test\",
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
                          \"email\": \"user@example.test\",
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
                          \"email\": \"user@example.test\",
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
    void shouldReturnTooManyRequestsWhenRegisterRegisterRateLimitIsExceeded() {
        when(registerCommandUseCase.handle(any()))
                .thenReturn(Mono.error(new RateLimitExceededException("Registration rate limit exceeded")));

        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          \"email\": \"register@example.test\",
                          \"password\": \"PlainSecret123\"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectBody()
                .jsonPath("$.code").isEqualTo("operacion_no_permitida");
    }

    @Test
    void shouldMapPublicRegisterEndpointToPublicSelfRegistrationMode() {
        when(registerCommandUseCase.handle(any()))
                .thenReturn(Mono.just(new RegisterResult("usr-public", "public@example.test", "ACTIVE")));

        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          \"email\": \"public@example.test\",
                          \"password\": \"PlainSecret123\"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo("usr-public")
                .jsonPath("$.email").isEqualTo("public@example.test")
                .jsonPath("$.status").isEqualTo("ACTIVE");

        ArgumentCaptor<RegisterCommand> commandCaptor = ArgumentCaptor.forClass(RegisterCommand.class);
        verify(registerCommandUseCase).handle(commandCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                io.identityaccess.domain.model.user.RegistrationMode.PUBLIC_SELF_REGISTRATION,
                commandCaptor.getValue().registrationMode());
    }

    @Test
    void shouldMapPrimaryRegisterEndpointToPrimaryRegistrationMode() {
        when(registerCommandUseCase.handle(any()))
                .thenReturn(Mono.just(new RegisterResult("usr-primary", "primary@example.test", "ACTIVE")));

        webTestClient.post()
                .uri("/api/v1/auth/register-primary")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          \"email\": \"primary@example.test\",
                          \"password\": \"PlainSecret123\"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo("usr-primary")
                .jsonPath("$.email").isEqualTo("primary@example.test")
                .jsonPath("$.status").isEqualTo("ACTIVE");

        ArgumentCaptor<RegisterCommand> commandCaptor = ArgumentCaptor.forClass(RegisterCommand.class);
        verify(registerCommandUseCase).handle(commandCaptor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(
                io.identityaccess.domain.model.user.RegistrationMode.PRIMARY_REGISTRATION,
                commandCaptor.getValue().registrationMode());
    }

    @Test
    void shouldReturnConflictWhenPrimaryAccountAlreadyExists() {
        when(registerCommandUseCase.handle(any()))
                .thenReturn(Mono.error(new PrimaryAccountAlreadyExistsException()));

        webTestClient.post()
                .uri("/api/v1/auth/register-primary")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          \"email\": \"primary@example.test\",
                          \"password\": \"PlainSecret123\"
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.code").isEqualTo("cuenta_primaria_ya_existe");
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
                        List.of("identity-access-client"),
                        1735689600L,
                        1735690500L,
                        "acc-jti-1",
                        "user@example.test",
                        Set.of("SYSTEM_ADMIN"),
                        Set.of("iam.account.read"))));

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
                .jsonPath("$.email").isEqualTo("user@example.test")
                .jsonPath("$.roles[0]").isEqualTo("SYSTEM_ADMIN")
                .jsonPath("$.permissions[0]").isEqualTo("iam.account.read");
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
                        Set.of("ACCESS_ADMIN"),
                        Set.of("iam.account.read", "iam.account.create"))));

        webTestClient.get()
                .uri("/api/v1/admin/iam/accounts/usr-5/permissions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userId").isEqualTo("usr-5")
                .jsonPath("$.roles[0]").isEqualTo("ACCESS_ADMIN")
                .jsonPath("$.permissions[0]").isNotEmpty();
    }

    @Test
    void shouldReturnAccountsResponse() {
        when(listAccountsQueryUseCase.handle())
                .thenReturn(reactor.core.publisher.Flux.just(new AccountSummaryResult(
                        "usr-1",
                        "admin@example.test",
                        "ACTIVE",
                        0,
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Instant.parse("2026-01-01T00:00:00Z"),
                        Set.of("SYSTEM_ADMIN"))));

        webTestClient.get()
                .uri("/api/v1/admin/iam/accounts")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].userId").isEqualTo("usr-1")
                .jsonPath("$[0].email").isEqualTo("admin@example.test")
                .jsonPath("$[0].status").isEqualTo("ACTIVE")
                .jsonPath("$[0].roles[0]").isEqualTo("SYSTEM_ADMIN");
    }

    @Test
    void shouldReturnSessionsResponse() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        when(listSessionsQueryUseCase.handle())
                .thenReturn(reactor.core.publisher.Flux.just(new SessionSummaryResult(
                        "ses-1",
                        "usr-1",
                        "ACTIVE",
                        "127.0.0.1",
                        "device-1",
                        "browser",
                        "WEB",
                        now,
                        now.plusSeconds(900),
                        now.plusSeconds(604800),
                        now,
                        null,
                        null,
                        now,
                        now)));

        webTestClient.get()
                .uri("/api/v1/admin/iam/sessions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].sessionId").isEqualTo("ses-1")
                .jsonPath("$[0].userId").isEqualTo("usr-1")
                .jsonPath("$[0].status").isEqualTo("ACTIVE")
                .jsonPath("$[0].ipAddress").isEqualTo("127.0.0.1");
    }

    @Test
    void shouldReturnRoleCatalogResponse() {
        when(accessCatalogCommandUseCase.listRoles())
                .thenReturn(reactor.core.publisher.Flux.just(new RoleCatalogResult(
                        "role-1",
                        "ACCESS_ADMIN",
                        "Access administrator",
                        "ACTIVE",
                        false)));

        webTestClient.get()
                .uri("/api/v1/admin/iam/roles")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].roleId").isEqualTo("role-1")
                .jsonPath("$[0].roleCode").isEqualTo("ACCESS_ADMIN")
                .jsonPath("$[0].status").isEqualTo("ACTIVE");
    }

    @Test
    void shouldReturnPermissionCatalogResponse() {
        when(accessCatalogCommandUseCase.listPermissions())
                .thenReturn(reactor.core.publisher.Flux.just(new PermissionCatalogResult(
                        "perm-1",
                        "iam.account.read",
                        "iam.account",
                        "read",
                        "GLOBAL",
                        "Read account",
                        "ACTIVE",
                        true)));

        webTestClient.get()
                .uri("/api/v1/admin/iam/permissions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].permissionId").isEqualTo("perm-1")
                .jsonPath("$[0].permissionCode").isEqualTo("iam.account.read")
                .jsonPath("$[0].resource").isEqualTo("iam.account")
                .jsonPath("$[0].action").isEqualTo("read");
    }

    @Test
    void shouldReturnRolePermissionsResponse() {
        when(accessCatalogCommandUseCase.listPermissionsByRoleId("role-1"))
                .thenReturn(reactor.core.publisher.Flux.just(new PermissionCatalogResult(
                        "perm-1",
                        "iam.account.read",
                        "iam.account",
                        "read",
                        "GLOBAL",
                        "Read account",
                        "ACTIVE",
                        true)));

        webTestClient.get()
                .uri("/api/v1/admin/iam/roles/role-1/permissions")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].permissionId").isEqualTo("perm-1")
                .jsonPath("$[0].permissionCode").isEqualTo("iam.account.read")
                .jsonPath("$[0].resource").isEqualTo("iam.account")
                .jsonPath("$[0].action").isEqualTo("read");
    }
}
