package io.identityaccess.infrastructure.adapter.in.web.controller;

import io.identityaccess.application.port.in.ListSessionsQueryUseCase;
import io.identityaccess.application.port.in.RevokeSessionsCommandUseCase;
import io.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.RevokeSessionsCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.RevokeSessionsResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.SessionSummaryResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.request.RevokeSessionsRequest;
import io.identityaccess.infrastructure.adapter.in.web.response.RevokeSessionsResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.SessionSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
public class SessionAdminHttpController {

    private final RevokeSessionsCommandMapper revokeSessionsCommandMapper;
    private final RevokeSessionsCommandUseCase revokeSessionsCommandUseCase;
    private final RevokeSessionsResponseMapper revokeSessionsResponseMapper;
    private final ListSessionsQueryUseCase listSessionsQueryUseCase;
    private final SessionSummaryResponseMapper sessionSummaryResponseMapper;

    public SessionAdminHttpController(
            RevokeSessionsCommandMapper revokeSessionsCommandMapper,
            RevokeSessionsCommandUseCase revokeSessionsCommandUseCase,
            RevokeSessionsResponseMapper revokeSessionsResponseMapper,
            ListSessionsQueryUseCase listSessionsQueryUseCase,
            SessionSummaryResponseMapper sessionSummaryResponseMapper) {
        this.revokeSessionsCommandMapper = revokeSessionsCommandMapper;
        this.revokeSessionsCommandUseCase = revokeSessionsCommandUseCase;
        this.revokeSessionsResponseMapper = revokeSessionsResponseMapper;
        this.listSessionsQueryUseCase = listSessionsQueryUseCase;
        this.sessionSummaryResponseMapper = sessionSummaryResponseMapper;
    }

    @PreAuthorize("hasAuthority('iam.account.read')")
    @GetMapping("/admin/iam/sessions")
    public Flux<SessionSummaryResponse> listSessions() {
        return listSessionsQueryUseCase.handle().map(sessionSummaryResponseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.session.revoke')")
    @PostMapping("/admin/iam/accounts/{userId}/sessions/revoke")
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
