package com.arka.identityaccess.infrastructure.adapter.in.web.controller;

import com.arka.identityaccess.application.port.in.AssignRoleCommandUseCase;
import com.arka.identityaccess.application.port.in.BlockUserCommandUseCase;
import com.arka.identityaccess.application.port.in.GetUserPermissionsQueryUseCase;
import com.arka.identityaccess.application.port.in.RegisterCommandUseCase;
import com.arka.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.AssignRoleCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.BlockUserCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.GetUserPermissionsQueryMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.command.RegisterCommandMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.AssignRoleResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.BlockUserResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.RegisterResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.mapper.response.UserPermissionsResponseMapper;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.AdminRegisterUserRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.AssignRoleRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.request.BlockUserRequest;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.AssignRoleResponse;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.BlockUserResponse;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.RegisterResponse;
import com.arka.identityaccess.infrastructure.adapter.in.web.response.UserPermissionsResponse;
import jakarta.validation.Valid;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
public class AdminIamHttpController {

    private final RegisterCommandMapper registerCommandMapper;
    private final RegisterCommandUseCase registerCommandUseCase;
    private final RegisterResponseMapper registerResponseMapper;
    private final AssignRoleCommandMapper assignRoleCommandMapper;
    private final AssignRoleCommandUseCase assignRoleCommandUseCase;
    private final AssignRoleResponseMapper assignRoleResponseMapper;
    private final BlockUserCommandMapper blockUserCommandMapper;
    private final BlockUserCommandUseCase blockUserCommandUseCase;
    private final BlockUserResponseMapper blockUserResponseMapper;
    private final GetUserPermissionsQueryMapper getUserPermissionsQueryMapper;
    private final GetUserPermissionsQueryUseCase getUserPermissionsQueryUseCase;
    private final UserPermissionsResponseMapper userPermissionsResponseMapper;

    public AdminIamHttpController(
            RegisterCommandMapper registerCommandMapper,
            RegisterCommandUseCase registerCommandUseCase,
            RegisterResponseMapper registerResponseMapper,
            AssignRoleCommandMapper assignRoleCommandMapper,
            AssignRoleCommandUseCase assignRoleCommandUseCase,
            AssignRoleResponseMapper assignRoleResponseMapper,
            BlockUserCommandMapper blockUserCommandMapper,
            BlockUserCommandUseCase blockUserCommandUseCase,
            BlockUserResponseMapper blockUserResponseMapper,
            GetUserPermissionsQueryMapper getUserPermissionsQueryMapper,
            GetUserPermissionsQueryUseCase getUserPermissionsQueryUseCase,
            UserPermissionsResponseMapper userPermissionsResponseMapper) {
        this.registerCommandMapper = registerCommandMapper;
        this.registerCommandUseCase = registerCommandUseCase;
        this.registerResponseMapper = registerResponseMapper;
        this.assignRoleCommandMapper = assignRoleCommandMapper;
        this.assignRoleCommandUseCase = assignRoleCommandUseCase;
        this.assignRoleResponseMapper = assignRoleResponseMapper;
        this.blockUserCommandMapper = blockUserCommandMapper;
        this.blockUserCommandUseCase = blockUserCommandUseCase;
        this.blockUserResponseMapper = blockUserResponseMapper;
        this.getUserPermissionsQueryMapper = getUserPermissionsQueryMapper;
        this.getUserPermissionsQueryUseCase = getUserPermissionsQueryUseCase;
        this.userPermissionsResponseMapper = userPermissionsResponseMapper;
    }

    @PreAuthorize("hasAuthority('iam.user.create')")
    @PostMapping("/admin/iam/users")
    public Mono<RegisterResponse> registerOrganizationUser(
            @Valid @RequestBody AdminRegisterUserRequest request,
            Authentication authentication,
            ServerHttpRequest httpRequest) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return registerCommandUseCase
                .handle(registerCommandMapper.toAdminCommand(request, principal, httpRequest))
                .map(registerResponseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.user.assign-role')")
    @PostMapping("/admin/iam/users/{userId}/assign-role")
    public Mono<AssignRoleResponse> assignRole(
            @PathVariable String userId,
            @Valid @RequestBody AssignRoleRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return assignRoleCommandUseCase
                .handle(assignRoleCommandMapper.toCommand(userId, request, principal))
                .map(assignRoleResponseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.user.block')")
    @PostMapping("/admin/iam/users/{userId}/block")
    public Mono<BlockUserResponse> blockUser(
            @PathVariable String userId,
            @Valid @RequestBody BlockUserRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return blockUserCommandUseCase
                .handle(blockUserCommandMapper.toCommand(userId, request, principal))
                .map(blockUserResponseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.permission.read')")
    @GetMapping("/admin/iam/users/{userId}/permissions")
    public Mono<UserPermissionsResponse> getUserPermissions(@PathVariable String userId) {
        return getUserPermissionsQueryUseCase
                .handle(getUserPermissionsQueryMapper.toQuery(userId))
                .map(userPermissionsResponseMapper::toResponse);
    }
}
