package com.arka.identityaccess.infrastructure.adapter.in.web.controller;

import com.arka.identityaccess.application.port.in.RevokeSessionsCommandUseCase;
import com.arka.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.RevokeSessionsCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.RevokeSessionsResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.RevokeSessionsRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.RevokeSessionsResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
public class SessionAdminHttpController {

    private final RevokeSessionsCommandMapper revokeSessionsCommandMapper;
    private final RevokeSessionsCommandUseCase revokeSessionsCommandUseCase;
    private final RevokeSessionsResponseMapper revokeSessionsResponseMapper;

    public SessionAdminHttpController(
            RevokeSessionsCommandMapper revokeSessionsCommandMapper,
            RevokeSessionsCommandUseCase revokeSessionsCommandUseCase,
            RevokeSessionsResponseMapper revokeSessionsResponseMapper) {
        this.revokeSessionsCommandMapper = revokeSessionsCommandMapper;
        this.revokeSessionsCommandUseCase = revokeSessionsCommandUseCase;
        this.revokeSessionsResponseMapper = revokeSessionsResponseMapper;
    }

    @PreAuthorize("hasAuthority('iam.user.revoke-session')")
    @PostMapping("/admin/iam/users/{userId}/sessions/revoke")
    public Mono<RevokeSessionsResponse> revokeSessions(
            @PathVariable String userId,
            @Valid @RequestBody RevokeSessionsRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return revokeSessionsCommandUseCase
                .handle(revokeSessionsCommandMapper.toCommand(userId, request, principal))
                .map(revokeSessionsResponseMapper::toResponse);
    }
}
