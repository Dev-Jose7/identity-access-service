package io.identityaccess.infrastructure.adapter.in.web.controller;

import io.identityaccess.application.port.in.IntrospectTokenQueryUseCase;
import io.identityaccess.infrastructure.adapter.in.web.mapper.query.IntrospectQueryMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.IntrospectResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.request.IntrospectRequest;
import io.identityaccess.infrastructure.adapter.in.web.response.IntrospectResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
public class TokenIntrospectController {

    private final IntrospectQueryMapper introspectQueryMapper;
    private final IntrospectTokenQueryUseCase introspectTokenQueryUseCase;
    private final IntrospectResponseMapper introspectResponseMapper;

    public TokenIntrospectController(
            IntrospectQueryMapper introspectQueryMapper,
            IntrospectTokenQueryUseCase introspectTokenQueryUseCase,
            IntrospectResponseMapper introspectResponseMapper) {
        this.introspectQueryMapper = introspectQueryMapper;
        this.introspectTokenQueryUseCase = introspectTokenQueryUseCase;
        this.introspectResponseMapper = introspectResponseMapper;
    }

    @PostMapping("/auth/introspect")
    public Mono<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest request) {
        return introspectTokenQueryUseCase
                .handle(introspectQueryMapper.toQuery(request))
                .map(introspectResponseMapper::toResponse);
    }
}
