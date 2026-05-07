package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.RegisterCommand;
import com.arka.identityaccess.application.mapper.command.RegisterCommandAssembler;
import com.arka.identityaccess.application.mapper.result.RegisterResultMapper;
import com.arka.identityaccess.application.port.in.RegisterCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.RoleAssignmentPolicyPort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.port.out.security.PasswordHashPort;
import com.arka.identityaccess.application.result.RegisterResult;
import com.arka.identityaccess.domain.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.exception.UserAlreadyExistsException;
import com.arka.identityaccess.domain.model.role.RoleCode;
import com.arka.identityaccess.domain.model.user.RegistrationMode;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.event.UserRegisteredEvent;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.service.UserRegistrationPolicy;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RegisterUseCase implements RegisterCommandUseCase {

    private final RegisterCommandAssembler assembler;
    private final SecurityRateLimitPort securityRateLimitPort;
    private final UserPersistencePort userPersistencePort;
    private final UserRegistrationPolicy registrationPolicy;
    private final RoleAssignmentPolicyPort roleAssignmentPolicyPort;
    private final PasswordHashPort passwordHashPort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final RegisterResultMapper resultMapper;

    public RegisterUseCase(
            RegisterCommandAssembler assembler,
            SecurityRateLimitPort securityRateLimitPort,
            UserPersistencePort userPersistencePort,
            UserRegistrationPolicy registrationPolicy,
            RoleAssignmentPolicyPort roleAssignmentPolicyPort,
            PasswordHashPort passwordHashPort,
            SecurityAuditPort securityAuditPort,
            OutboxPersistencePort outboxPersistencePort,
            RegisterResultMapper resultMapper) {
        this.assembler = assembler;
        this.securityRateLimitPort = securityRateLimitPort;
        this.userPersistencePort = userPersistencePort;
        this.registrationPolicy = registrationPolicy;
        this.roleAssignmentPolicyPort = roleAssignmentPolicyPort;
        this.passwordHashPort = passwordHashPort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.resultMapper = resultMapper;
    }

    @Override
    public Mono<RegisterResult> handle(RegisterCommand command) {
        EmailAddress email = assembler.toEmailAddress(command);
        UserRegistrationPolicy.RegistrationDecision decision = registrationPolicy.evaluate(
                command.registrationMode(),
                command.requestedRoleCode(),
                command.actorId());
        return enforceFounderRateLimitIfRequired(command, email)
                .then(authorizeRoleAssignmentIfRequired(command.actorId(), decision))
                .flatMap(authorizedDecision -> register(command, email, authorizedDecision.targetRoleCode()));
    }

    private Mono<Void> enforceFounderRateLimitIfRequired(RegisterCommand command, EmailAddress email) {
        if (command.registrationMode() != RegistrationMode.ONBOARDING_OWNER) {
            return Mono.empty();
        }
        return securityRateLimitPort.ensureFounderRegistrationAllowed(email, ClientIp.of(command.ipAddress()));
    }

    private Mono<UserRegistrationPolicy.RegistrationDecision> authorizeRoleAssignmentIfRequired(
            String actorId,
            UserRegistrationPolicy.RegistrationDecision decision) {
        if (!decision.requiresActorAssignmentValidation()) {
            return Mono.just(decision);
        }

        return roleAssignmentPolicyPort
                .canAssignRole(actorId, decision.targetRoleCode().name())
                .flatMap(canAssign -> canAssign
                        ? Mono.just(decision)
                        : Mono.error(new OperationNotPermittedException("Actor cannot assign requested role")));
    }

    private String assignedBy(RegisterCommand command) {
        if (command.registrationMode() == RegistrationMode.ONBOARDING_OWNER) {
            return "SYSTEM_ONBOARDING";
        }
        return command.actorId();
    }

    private Mono<RegisterResult> register(RegisterCommand command, EmailAddress email, RoleCode targetRoleCode) {
        return Mono.defer(() -> userPersistencePort.existsByEmail(email))
                .flatMap(exists -> exists ? Mono.error(new UserAlreadyExistsException()) : Mono.empty())
                .then(Mono.defer(() -> passwordHashPort.hash(command.rawPassword())))
                .map(passwordHash -> UserAggregate.register(email, passwordHash, Set.of(targetRoleCode.name())))
                .flatMap(user -> userPersistencePort.create(user, targetRoleCode.name(), assignedBy(command)))
                .flatMap(savedUser -> {
                    UserRegisteredEvent event = UserRegisteredEvent.create(
                            savedUser.id(),
                            savedUser.email(),
                            Instant.now());
                    return securityAuditPort.recordUserRegistered(savedUser)
                            .then(outboxPersistencePort.store(event))
                            .thenReturn(resultMapper.toResult(savedUser));
                });
    }
}
