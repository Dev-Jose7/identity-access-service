package io.identityaccess.infrastructure.adapter.in.web.mapper.command;

import io.identityaccess.application.command.RegisterCommand;
import io.identityaccess.domain.model.user.RegistrationMode;
import io.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import io.identityaccess.infrastructure.adapter.in.web.request.AdminRegisterUserRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.RegisterRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RegisterCommandMapper {

    public RegisterCommand toPublicRegistrationCommand(RegisterRequest request, ServerHttpRequest httpRequest) {
        return toRegistrationCommand(
                request.email(),
                request.password(),
                RegistrationMode.PUBLIC_SELF_REGISTRATION,
                httpRequest);
    }

    public RegisterCommand toPrimaryRegistrationCommand(RegisterRequest request, ServerHttpRequest httpRequest) {
        return toRegistrationCommand(
                request.email(),
                request.password(),
                RegistrationMode.PRIMARY_REGISTRATION,
                httpRequest);
    }

    private RegisterCommand toRegistrationCommand(
            String email,
            String password,
            RegistrationMode registrationMode,
            ServerHttpRequest httpRequest) {
        return new RegisterCommand(
                email,
                password,
                registrationMode,
                null,
                null,
                resolveUserAgent(httpRequest),
                resolveClientIp(httpRequest));
    }

    public RegisterCommand toAdminCommand(AdminRegisterUserRequest request, IamSecurityPrincipal principal, ServerHttpRequest httpRequest) {
        return new RegisterCommand(
                request.email(),
                request.password(),
                RegistrationMode.ADMIN_CREATE,
                request.roleCode(),
                principal.userId(),
                resolveUserAgent(httpRequest),
                resolveClientIp(httpRequest));
    }

    private String resolveClientIp(ServerHttpRequest httpRequest) {
        String forwardedFor = httpRequest.getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = httpRequest.getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        if (httpRequest.getRemoteAddress() != null && httpRequest.getRemoteAddress().getAddress() != null) {
            return httpRequest.getRemoteAddress().getAddress().getHostAddress();
        }

        return "127.0.0.1";
    }

    private String resolveUserAgent(ServerHttpRequest httpRequest) {
        String userAgent = httpRequest.getHeaders().getFirst("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return "unknown";
        }
        return userAgent.trim();
    }
}
