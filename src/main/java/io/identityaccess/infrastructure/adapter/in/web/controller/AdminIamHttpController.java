package io.identityaccess.infrastructure.adapter.in.web.controller;

import io.identityaccess.application.port.in.AssignRoleCommandUseCase;
import io.identityaccess.application.port.in.BlockUserCommandUseCase;
import io.identityaccess.application.port.in.GetUserPermissionsQueryUseCase;
import io.identityaccess.application.port.in.ListAccountsQueryUseCase;
import io.identityaccess.application.port.in.RegisterCommandUseCase;
import io.identityaccess.application.port.in.UnblockUserCommandUseCase;
import io.identityaccess.infrastructure.adapter.in.security.IamSecurityPrincipal;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.AssignRoleCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.BlockUserCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.GetUserPermissionsQueryMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.RegisterCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.command.UnblockUserCommandMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.AssignRoleResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.BlockUserResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.AccountSummaryResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.RegisterResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.UnblockUserResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.mapper.response.UserPermissionsResponseMapper;
import io.identityaccess.infrastructure.adapter.in.web.request.AdminRegisterUserRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.AssignRoleRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.BlockUserRequest;
import io.identityaccess.infrastructure.adapter.in.web.request.UnblockUserRequest;
import io.identityaccess.infrastructure.adapter.in.web.response.AccountSummaryResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.AssignRoleResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.BlockUserResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.RegisterResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.UnblockUserResponse;
import io.identityaccess.infrastructure.adapter.in.web.response.UserPermissionsResponse;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1")
public class AdminIamHttpController {

    private final RegisterCommandMapper registerCommandMapper;
    private final RegisterCommandUseCase registerCommandUseCase;
    private final RegisterResponseMapper registerResponseMapper;
    private final ListAccountsQueryUseCase listAccountsQueryUseCase;
    private final AccountSummaryResponseMapper accountSummaryResponseMapper;
    private final AssignRoleCommandMapper assignRoleCommandMapper;
    private final AssignRoleCommandUseCase assignRoleCommandUseCase;
    private final AssignRoleResponseMapper assignRoleResponseMapper;
    private final BlockUserCommandMapper blockUserCommandMapper;
    private final BlockUserCommandUseCase blockUserCommandUseCase;
    private final BlockUserResponseMapper blockUserResponseMapper;
    private final UnblockUserCommandMapper unblockUserCommandMapper;
    private final UnblockUserCommandUseCase unblockUserCommandUseCase;
    private final UnblockUserResponseMapper unblockUserResponseMapper;
    private final GetUserPermissionsQueryMapper getUserPermissionsQueryMapper;
    private final GetUserPermissionsQueryUseCase getUserPermissionsQueryUseCase;
    private final UserPermissionsResponseMapper userPermissionsResponseMapper;

    public AdminIamHttpController(
            RegisterCommandMapper registerCommandMapper,
            RegisterCommandUseCase registerCommandUseCase,
            RegisterResponseMapper registerResponseMapper,
            ListAccountsQueryUseCase listAccountsQueryUseCase,
            AccountSummaryResponseMapper accountSummaryResponseMapper,
            AssignRoleCommandMapper assignRoleCommandMapper,
            AssignRoleCommandUseCase assignRoleCommandUseCase,
            AssignRoleResponseMapper assignRoleResponseMapper,
            BlockUserCommandMapper blockUserCommandMapper,
            BlockUserCommandUseCase blockUserCommandUseCase,
            BlockUserResponseMapper blockUserResponseMapper,
            UnblockUserCommandMapper unblockUserCommandMapper,
            UnblockUserCommandUseCase unblockUserCommandUseCase,
            UnblockUserResponseMapper unblockUserResponseMapper,
            GetUserPermissionsQueryMapper getUserPermissionsQueryMapper,
            GetUserPermissionsQueryUseCase getUserPermissionsQueryUseCase,
            UserPermissionsResponseMapper userPermissionsResponseMapper) {
        this.registerCommandMapper = registerCommandMapper;
        this.registerCommandUseCase = registerCommandUseCase;
        this.registerResponseMapper = registerResponseMapper;
        this.listAccountsQueryUseCase = listAccountsQueryUseCase;
        this.accountSummaryResponseMapper = accountSummaryResponseMapper;
        this.assignRoleCommandMapper = assignRoleCommandMapper;
        this.assignRoleCommandUseCase = assignRoleCommandUseCase;
        this.assignRoleResponseMapper = assignRoleResponseMapper;
        this.blockUserCommandMapper = blockUserCommandMapper;
        this.blockUserCommandUseCase = blockUserCommandUseCase;
        this.blockUserResponseMapper = blockUserResponseMapper;
        this.unblockUserCommandMapper = unblockUserCommandMapper;
        this.unblockUserCommandUseCase = unblockUserCommandUseCase;
        this.unblockUserResponseMapper = unblockUserResponseMapper;
        this.getUserPermissionsQueryMapper = getUserPermissionsQueryMapper;
        this.getUserPermissionsQueryUseCase = getUserPermissionsQueryUseCase;
        this.userPermissionsResponseMapper = userPermissionsResponseMapper;
    }

    @PreAuthorize("hasAuthority('iam.account.read')")
    @GetMapping("/admin/iam/accounts")
    public Flux<AccountSummaryResponse> listAccounts() {
        return listAccountsQueryUseCase.handle().map(accountSummaryResponseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.account.create')")
    @PostMapping("/admin/iam/accounts")
    public Mono<RegisterResponse> createAccount(
            @Valid @RequestBody AdminRegisterUserRequest request,
            Authentication authentication,
            ServerHttpRequest httpRequest) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return registerCommandUseCase
                .handle(registerCommandMapper.toAdminCommand(request, principal, httpRequest))
                .map(registerResponseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.access.assign-role')")
    @PostMapping("/admin/iam/accounts/{userId}/roles")
    public Mono<AssignRoleResponse> assignRole(
            @PathVariable String userId,
            @Valid @RequestBody AssignRoleRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return assignRoleCommandUseCase
                .handle(assignRoleCommandMapper.toCommand(userId, request, principal))
                .map(assignRoleResponseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.account.block')")
    @PostMapping("/admin/iam/accounts/{userId}/block")
    public Mono<BlockUserResponse> blockUser(
            @PathVariable String userId,
            @Valid @RequestBody BlockUserRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return blockUserCommandUseCase
                .handle(blockUserCommandMapper.toCommand(userId, request, principal))
                .map(blockUserResponseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.account.unblock')")
    @PostMapping("/admin/iam/accounts/{userId}/unblock")
    public Mono<UnblockUserResponse> unblockUser(
            @PathVariable String userId,
            @Valid @RequestBody UnblockUserRequest request,
            Authentication authentication) {
        IamSecurityPrincipal principal = IamSecurityPrincipal.fromAuthentication(authentication);
        return unblockUserCommandUseCase
                .handle(unblockUserCommandMapper.toCommand(userId, request, principal))
                .map(unblockUserResponseMapper::toResponse);
    }

    @PreAuthorize("hasAuthority('iam.permission.read')")
    @GetMapping("/admin/iam/accounts/{userId}/permissions")
    public Mono<UserPermissionsResponse> getUserPermissions(@PathVariable String userId) {
        return getUserPermissionsQueryUseCase
                .handle(getUserPermissionsQueryMapper.toQuery(userId))
                .map(userPermissionsResponseMapper::toResponse);
    }
}
