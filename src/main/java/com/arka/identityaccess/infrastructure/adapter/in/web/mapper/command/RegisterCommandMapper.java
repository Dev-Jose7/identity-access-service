package com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command;

import com.arka.identityaccess.application.command.RegisterCommand;
import com.arka.identityaccess.domain.identity.enumtype.RegistrationMode;
import com.arka.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.AdminRegisterUserRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.RegisterFounderRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RegisterCommandMapper {

    public RegisterCommand toOnboardingCommand(RegisterFounderRequest request, ServerHttpRequest httpRequest) {
        return new RegisterCommand(
                request.email(),
                request.password(),
                RegistrationMode.ONBOARDING_OWNER,
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
