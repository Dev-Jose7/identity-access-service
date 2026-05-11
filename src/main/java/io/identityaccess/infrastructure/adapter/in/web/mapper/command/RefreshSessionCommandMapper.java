package io.identityaccess.infrastructure.adapter.in.web.mapper.command;

import io.identityaccess.application.command.RefreshSessionCommand;
import io.identityaccess.infrastructure.adapter.in.web.request.RefreshSessionRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RefreshSessionCommandMapper {
    public RefreshSessionCommand toCommand(RefreshSessionRequest request, ServerHttpRequest httpRequest) {
        return new RefreshSessionCommand(request.refreshToken(), resolveClientIp(httpRequest));
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
}
