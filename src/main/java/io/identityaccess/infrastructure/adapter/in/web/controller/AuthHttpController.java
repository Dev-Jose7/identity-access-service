package io.identityaccess.infrastructure.adapter.in.web.controller;

import io.identityaccess.application.command.LogoutCommand;
import io.identityaccess.application.port.in.LoginCommandUseCase;
import io.identityaccess.application.port.in.LogoutCommandUseCase;
import io.identityaccess.application.port.in.RefreshSessionCommandUseCase;
import io.identityaccess.application.port.in.RegisterCommandUseCase;
import io.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.LoginCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.RefreshSessionCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.RegisterCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.LoginResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.LogoutResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.RegisterResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.TokenPairResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.request.LoginRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.RefreshSessionRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.RegisterRequest;
import io.identityaccess.infrastructure.adapter.in.web.response.LoginResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.LogoutResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.RegisterResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.TokenPairResponse;
import jakarta.validation.Valid;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
public class AuthHttpController {

    private final LoginCommandMapper loginCommandMapper;
    private final LoginCommandUseCase loginCommandUseCase;
    private final LoginResponseMapper loginResponseMapper;
    private final RegisterCommandMapper registerCommandMapper;
    private final RegisterCommandUseCase registerCommandUseCase;
    private final RegisterResponseMapper registerResponseMapper;
    private final RefreshSessionCommandMapper refreshSessionCommandMapper;
    private final RefreshSessionCommandUseCase refreshSessionCommandUseCase;
    private final TokenPairResponseMapper tokenPairResponseMapper;
    private final LogoutCommandUseCase logoutCommandUseCase;
    private final LogoutResponseMapper logoutResponseMapper;

    public AuthHttpController(
            LoginCommandMapper loginCommandMapper,
            LoginCommandUseCase loginCommandUseCase,
            LoginResponseMapper loginResponseMapper,
            RegisterCommandMapper registerCommandMapper,
            RegisterCommandUseCase registerCommandUseCase,
            RegisterResponseMapper registerResponseMapper,
            RefreshSessionCommandMapper refreshSessionCommandMapper,
            RefreshSessionCommandUseCase refreshSessionCommandUseCase,
            TokenPairResponseMapper tokenPairResponseMapper,
            LogoutCommandUseCase logoutCommandUseCase,
            LogoutResponseMapper logoutResponseMapper) {
        this.loginCommandMapper = loginCommandMapper;
        this.loginCommandUseCase = loginCommandUseCase;
        this.loginResponseMapper = loginResponseMapper;
        this.registerCommandMapper = registerCommandMapper;
        this.registerCommandUseCase = registerCommandUseCase;
        this.registerResponseMapper = registerResponseMapper;
        this.refreshSessionCommandMapper = refreshSessionCommandMapper;
        this.refreshSessionCommandUseCase = refreshSessionCommandUseCase;
        this.tokenPairResponseMapper = tokenPairResponseMapper;
        this.logoutCommandUseCase = logoutCommandUseCase;
        this.logoutResponseMapper = logoutResponseMapper;
    }

    @PostMapping("/auth/register")
    public Mono<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request,
            ServerHttpRequest httpRequest) {
        return registerCommandUseCase
                .handle(registerCommandMapper.toPublicRegistrationCommand(request, httpRequest))
                .map(registerResponseMapper::toResponse);
    }

    @PostMapping("/auth/register-primary")
    public Mono<RegisterResponse> registerPrimary(
            @Valid @RequestBody RegisterRequest request,
            ServerHttpRequest httpRequest) {
        return registerCommandUseCase
                .handle(registerCommandMapper.toPrimaryRegistrationCommand(request, httpRequest))
                .map(registerResponseMapper::toResponse);
    }

    @PostMapping("/auth/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request, ServerHttpRequest httpRequest) {
        return loginCommandUseCase
                .handle(loginCommandMapper.toCommand(request, httpRequest))
                .map(loginResponseMapper::toResponse);
    }

    @PostMapping("/auth/refresh")
    public Mono<TokenPairResponse> refresh(
            @Valid @RequestBody RefreshSessionRequest request,
            ServerHttpRequest httpRequest) {
        return refreshSessionCommandUseCase
                .handle(refreshSessionCommandMapper.toCommand(request, httpRequest))
                .map(tokenPairResponseMapper::toResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/auth/logout")
    public Mono<LogoutResponse> logout(Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return logoutCommandUseCase
                .handle(new LogoutCommand(principal.sessionId()))
                .map(logoutResponseMapper::toResponse);
    }
}
