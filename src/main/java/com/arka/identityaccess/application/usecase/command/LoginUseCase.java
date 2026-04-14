package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.LoginCommand;
import com.arka.identityaccess.application.mapper.command.LoginCommandAssembler;
import com.arka.identityaccess.application.mapper.result.LoginResultMapper;
import com.arka.identityaccess.application.port.in.LoginCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.application.port.out.security.PasswordHashPort;
import com.arka.identityaccess.application.result.LoginResult;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.domain.identity.entity.AuthenticationAttempt;
import com.arka.identityaccess.domain.identity.exception.AccountNotEnabledException;
import com.arka.identityaccess.domain.identity.exception.CredentialNotUsableException;
import com.arka.identityaccess.domain.identity.exception.InvalidCredentialsException;
import com.arka.identityaccess.domain.identity.service.AuthenticationSecurityPolicy;
import com.arka.identityaccess.domain.identity.valueobject.AccountId;
import com.arka.identityaccess.domain.identity.valueobject.AuthenticationResult;
import com.arka.identityaccess.domain.identity.valueobject.AuthenticationSecurityRules;
import com.arka.identityaccess.domain.identity.valueobject.EmailAddress;
import com.arka.identityaccess.domain.shared.event.DomainEvent;
import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.session.service.TokenPolicy;
import java.time.Instant;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LoginUseCase implements LoginCommandUseCase {

    private final LoginCommandAssembler loginCommandAssembler;
    private final SecurityRateLimitPort securityRateLimitPort;
    private final UserPersistencePort userPersistencePort;
    private final PasswordHashPort passwordHashPort;
    private final AuthenticationSecurityPolicy authenticationSecurityPolicy;
    private final ClockPort clockPort;
    private final TokenPolicy tokenPolicy;
    private final SessionPersistencePort sessionPersistencePort;
    private final JwtSigningPort jwtSigningPort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final LoginResultMapper loginResultMapper;

    public LoginUseCase(
            LoginCommandAssembler loginCommandAssembler,
            SecurityRateLimitPort securityRateLimitPort,
            UserPersistencePort userPersistencePort,
            PasswordHashPort passwordHashPort,
            AuthenticationSecurityPolicy authenticationSecurityPolicy,
            ClockPort clockPort,
            TokenPolicy tokenPolicy,
            SessionPersistencePort sessionPersistencePort,
            JwtSigningPort jwtSigningPort,
            SecurityAuditPort securityAuditPort,
            OutboxPersistencePort outboxPersistencePort,
            LoginResultMapper loginResultMapper) {
        this.loginCommandAssembler = loginCommandAssembler;
        this.securityRateLimitPort = securityRateLimitPort;
        this.userPersistencePort = userPersistencePort;
        this.passwordHashPort = passwordHashPort;
        this.authenticationSecurityPolicy = authenticationSecurityPolicy;
        this.clockPort = clockPort;
        this.tokenPolicy = tokenPolicy;
        this.sessionPersistencePort = sessionPersistencePort;
        this.jwtSigningPort = jwtSigningPort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.loginResultMapper = loginResultMapper;
    }

    @Override
    public Mono<LoginResult> handle(LoginCommand command) {
        ensureRawPasswordProvided(command.rawPassword());
        EmailAddress email = loginCommandAssembler.toEmailAddress(command);
        ClientDevice clientDevice = loginCommandAssembler.toClientDevice(command);
        ClientIp clientIp = loginCommandAssembler.toClientIp(command);

        return securityRateLimitPort
                .ensureLoginAllowed(email, clientIp)
                .then(Mono.defer(() -> userPersistencePort.loadForLogin(email)))
                .flatMap(user -> authenticateAndOpenSession(user, command.rawPassword(), clientDevice, clientIp))
                .flatMap(context -> sessionPersistencePort.create(context.session()).map(savedSession -> new MaterializedLoginContext(context.user(), savedSession)))
                .flatMap(context -> userPersistencePort.loadAuthorizationSnapshot(context.user().id())
                        .flatMap(accessProfile -> Mono.zip(
                                        jwtSigningPort.signAccessToken(context.session(), accessProfile),
                                        jwtSigningPort.signRefreshToken(context.session()))
                                .flatMap(tokens -> securityAuditPort.recordLoginSuccess(context.user(), context.session())
                                        .then(publishDomainEvents(context.session().pullDomainEvents()))
                                        .thenReturn(loginResultMapper.toResult(context.session(), tokens.getT1(), tokens.getT2())))));
    }

    private Mono<AuthenticatedLoginContext> authenticateAndOpenSession(AccountAggregate account, String rawPassword, ClientDevice clientDevice, ClientIp clientIp) {
        return passwordHashPort.matches(rawPassword, account.credential().passwordHash())
                .flatMap(passwordMatches -> {
                    Instant now = clockPort.now();
                    AuthenticationSecurityRules securityRules = authenticationSecurityPolicy.rules();
                    AuthenticationResult authenticationResult =
                            account.authenticate(passwordMatches, securityRules, now, clientIp.value());
                    Mono<Void> attemptPersistence = persistLoginAttempt(account, authenticationResult.attempt());

                    if (!authenticationResult.success()) {
                        return attemptPersistence.then(Mono.error(toAuthenticationException(authenticationResult.failureCode())));
                    }

                    SessionAggregate session = SessionAggregate.open(
                            AccountId.of(account.id().value()),
                            clientDevice,
                            clientIp,
                            now,
                            tokenPolicy.calculateAccessExpiry(now),
                            tokenPolicy.calculateRefreshExpiry(now));
                    return attemptPersistence.thenReturn(new AuthenticatedLoginContext(account, session));
                });
    }

    private Mono<Void> persistLoginAttempt(AccountAggregate account, AuthenticationAttempt attempt) {
        if (attempt == null) {
            return Mono.empty();
        }
        return userPersistencePort.recordLoginAttempt(account.id(), attempt);
    }

    private RuntimeException toAuthenticationException(String failureCode) {
        return switch (failureCode) {
            case "account_not_enabled", "email_not_verified" -> new AccountNotEnabledException();
            case "credential_not_usable" -> new CredentialNotUsableException();
            case "invalid_credentials" -> new InvalidCredentialsException();
            default -> new InvalidCredentialsException();
        };
    }

    private void ensureRawPasswordProvided(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidCredentialsException();
        }
    }

    private Mono<Void> publishDomainEvents(Iterable<? extends DomainEvent> domainEvents) {
        return Flux.fromIterable(domainEvents).concatMap(outboxPersistencePort::store).then();
    }

    private record AuthenticatedLoginContext(AccountAggregate user, SessionAggregate session) {}
    private record MaterializedLoginContext(AccountAggregate user, SessionAggregate session) {}
}
