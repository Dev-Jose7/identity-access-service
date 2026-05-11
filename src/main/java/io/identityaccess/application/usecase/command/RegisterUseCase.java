package io.identityaccess.application.usecase.command;

import io.identityaccess.application.command.RegisterCommand;
import io.identityaccess.application.mapper.command.RegisterCommandAssembler;
import io.identityaccess.application.mapper.result.RegisterResultMapper;
import io.identityaccess.application.port.in.RegisterCommandUseCase;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.RoleAssignmentPolicyPort;
import io.identityaccess.application.port.out.persistence.UserPersistencePort;
import io.identityaccess.application.port.out.security.PasswordHashPort;
import io.identityaccess.application.result.RegisterResult;
import io.identityaccess.domain.exception.OperationNotPermittedException;
import io.identityaccess.domain.exception.PrimaryAccountAlreadyExistsException;
import io.identityaccess.domain.exception.UserAlreadyExistsException;
import io.identityaccess.domain.model.role.RoleCode;
import io.identityaccess.domain.model.user.RegistrationMode;
import io.identityaccess.domain.model.user.UserAggregate;
import io.identityaccess.domain.model.user.event.AccountRegisteredEvent;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.service.UserRegistrationPolicy;
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
    private final ClockPort clockPort;
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
            ClockPort clockPort,
            RegisterResultMapper resultMapper) {
        this.assembler = assembler;
        this.securityRateLimitPort = securityRateLimitPort;
        this.userPersistencePort = userPersistencePort;
        this.registrationPolicy = registrationPolicy;
        this.roleAssignmentPolicyPort = roleAssignmentPolicyPort;
        this.passwordHashPort = passwordHashPort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.clockPort = clockPort;
        this.resultMapper = resultMapper;
    }

    @Override
    public Mono<RegisterResult> handle(RegisterCommand command) {
        EmailAddress email = assembler.toEmailAddress(command);
        UserRegistrationPolicy.RegistrationDecision decision = registrationPolicy.evaluate(
                command.registrationMode(),
                command.requestedRoleCode(),
                command.actorId());
        return enforcePublicRegistrationRateLimitIfRequired(command, email)
                .then(rejectDuplicatePrimaryRegistrationIfRequired(decision))
                .then(authorizeRoleAssignmentIfRequired(command.actorId(), decision))
                .flatMap(authorizedDecision -> register(command, email, authorizedDecision.targetRoleCode()));
    }

    private Mono<Void> enforcePublicRegistrationRateLimitIfRequired(RegisterCommand command, EmailAddress email) {
        if (command.registrationMode() != RegistrationMode.PUBLIC_SELF_REGISTRATION
                && command.registrationMode() != RegistrationMode.PRIMARY_REGISTRATION) {
            return Mono.empty();
        }
        return securityRateLimitPort.ensureRegistrationAllowed(email, ClientIp.of(command.ipAddress()));
    }

    private Mono<Void> rejectDuplicatePrimaryRegistrationIfRequired(
            UserRegistrationPolicy.RegistrationDecision decision) {
        if (!decision.primaryRegistration()) {
            return Mono.empty();
        }
        return userPersistencePort
                .existsByRoleCode(decision.targetRoleCode().value())
                .flatMap(exists -> exists
                        ? Mono.error(new PrimaryAccountAlreadyExistsException())
                        : Mono.empty());
    }

    private Mono<UserRegistrationPolicy.RegistrationDecision> authorizeRoleAssignmentIfRequired(
            String actorId,
            UserRegistrationPolicy.RegistrationDecision decision) {
        if (!decision.requiresActorAssignmentValidation()) {
            return Mono.just(decision);
        }

        return roleAssignmentPolicyPort
                .canAssignRole(actorId, decision.targetRoleCode().value())
                .flatMap(canAssign -> canAssign
                        ? Mono.just(decision)
                        : Mono.error(new OperationNotPermittedException("Actor cannot assign requested role")));
    }

    private String assignedBy(RegisterCommand command) {
        if (command.registrationMode() == RegistrationMode.PRIMARY_REGISTRATION) {
            return "SYSTEM_PRIMARY_REGISTRATION";
        }
        if (command.registrationMode() == RegistrationMode.PUBLIC_SELF_REGISTRATION) {
            return "SYSTEM_REGISTRATION";
        }
        return command.actorId();
    }

    private Mono<RegisterResult> register(RegisterCommand command, EmailAddress email, RoleCode targetRoleCode) {
        return Mono.defer(() -> userPersistencePort.existsByEmail(email))
                .flatMap(exists -> exists ? Mono.error(new UserAlreadyExistsException()) : Mono.empty())
                .then(Mono.defer(() -> passwordHashPort.hash(command.rawPassword())))
                .map(passwordHash -> UserAggregate.register(email, passwordHash, Set.of(targetRoleCode.value())))
                .flatMap(user -> userPersistencePort.create(user, targetRoleCode.value(), assignedBy(command)))
                .flatMap(savedUser -> {
                    AccountRegisteredEvent event = AccountRegisteredEvent.create(
                            savedUser.id(),
                            savedUser.email(),
                            clockPort.now());
                    return securityAuditPort.recordAccountRegistered(savedUser)
                            .then(outboxPersistencePort.store(event))
                            .thenReturn(resultMapper.toResult(savedUser));
                });
    }
}
