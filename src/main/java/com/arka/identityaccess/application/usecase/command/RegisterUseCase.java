package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.RegisterCommand;
import com.arka.identityaccess.application.mapper.command.RegisterCommandAssembler;
import com.arka.identityaccess.application.mapper.result.RegisterResultMapper;
import com.arka.identityaccess.application.port.in.RegisterCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.port.out.security.PasswordHashPort;
import com.arka.identityaccess.application.result.RegisterResult;
import com.arka.identityaccess.application.exception.AccountAlreadyExistsException;
import com.arka.identityaccess.domain.access.service.InitialAccessProvisioningPolicy;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.enumtype.RegistrationMode;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.shared.exception.OperationNotPermittedException;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RegisterUseCase implements RegisterCommandUseCase {

    private final RegisterCommandAssembler assembler;
    private final SecurityRateLimitPort securityRateLimitPort;
    private final UserPersistencePort userPersistencePort;
    private final InitialAccessProvisioningPolicy registrationPolicy;
    private final PasswordHashPort passwordHashPort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final ClockPort clockPort;
    private final RegisterResultMapper resultMapper;

    public RegisterUseCase(
            RegisterCommandAssembler assembler,
            SecurityRateLimitPort securityRateLimitPort,
            UserPersistencePort userPersistencePort,
            InitialAccessProvisioningPolicy registrationPolicy,
            PasswordHashPort passwordHashPort,
            SecurityAuditPort securityAuditPort,
            OutboxPersistencePort outboxPersistencePort,
            ClockPort clockPort,
            RegisterResultMapper resultMapper) {
        this.assembler = assembler;
        this.securityRateLimitPort = securityRateLimitPort;
        this.userPersistencePort = userPersistencePort;
        this.registrationPolicy = registrationPolicy;
        this.passwordHashPort = passwordHashPort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.clockPort = clockPort;
        this.resultMapper = resultMapper;
    }

    @Override
    public Mono<RegisterResult> handle(RegisterCommand command) {
        EmailAddress email = assembler.toEmailAddress(command);
        String actorId = requireActorIfAdminCreate(command);
        InitialAccessProvisioningPolicy.ProvisioningDecision decision = registrationPolicy.resolveInitialAccess(
                command.registrationMode(),
                command.requestedRoleCode());
        return enforceFounderRateLimitIfRequired(command, email)
                .then(authorizeRoleAssignmentIfRequired(actorId, decision))
                .flatMap(authorizedDecision -> register(command, email, authorizedDecision.targetRoleCode()));
    }

    private Mono<Void> enforceFounderRateLimitIfRequired(RegisterCommand command, EmailAddress email) {
        if (command.registrationMode() != RegistrationMode.ONBOARDING_OWNER) {
            return Mono.empty();
        }
        return securityRateLimitPort.ensureFounderRegistrationAllowed(email, ClientIp.of(command.ipAddress()));
    }

    private Mono<InitialAccessProvisioningPolicy.ProvisioningDecision> authorizeRoleAssignmentIfRequired(
            String actorId,
            InitialAccessProvisioningPolicy.ProvisioningDecision decision) {
        if (!decision.requiresActorAssignmentValidation()) {
            return Mono.just(decision);
        }

        return userPersistencePort
                .canAssignRole(AccountId.of(actorId), decision.targetRoleCode())
                .flatMap(canAssign -> canAssign
                        ? Mono.just(decision)
                        : Mono.error(new OperationNotPermittedException("Actor cannot assign requested role")));
    }

    private String assignedBy(RegisterCommand command) {
        if (command.registrationMode() == RegistrationMode.ONBOARDING_OWNER) {
            return "SYSTEM_ONBOARDING";
        }
        return requireActorIfAdminCreate(command);
    }

    private Mono<RegisterResult> register(RegisterCommand command, EmailAddress email, RoleCode targetRoleCode) {
        return Mono.defer(() -> userPersistencePort.existsByEmail(email))
                .flatMap(exists -> exists ? Mono.error(new AccountAlreadyExistsException()) : Mono.empty())
                .then(Mono.defer(() -> passwordHashPort.hash(command.rawPassword())))
                .flatMap(passwordHash -> {
                    AccountAggregate account = AccountAggregate.register(email, passwordHash, false, clockPort.now());
                    var domainEvents = account.pullDomainEvents();
                    return userPersistencePort.create(account, targetRoleCode, assignedBy(command))
                            .flatMap(savedAccount -> securityAuditPort.recordUserRegistered(savedAccount)
                                    .then(publishDomainEvents(domainEvents))
                                    .thenReturn(resultMapper.toResult(savedAccount)));
                });
    }

    private String requireActorIfAdminCreate(RegisterCommand command) {
        if (command.registrationMode() != RegistrationMode.ADMIN_CREATE) {
            return command.actorId();
        }
        if (command.actorId() == null || command.actorId().isBlank()) {
            throw new OperationNotPermittedException("Register requires authenticated actor context");
        }
        return command.actorId().trim();
    }

    private Mono<Void> publishDomainEvents(Iterable<? extends DomainEvent> domainEvents) {
        return Flux.fromIterable(domainEvents).concatMap(outboxPersistencePort::store).then();
    }
}
