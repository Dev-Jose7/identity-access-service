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
import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.valueobject.ClientDevice;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.domain.model.user.valueobject.EmailAddress;
import com.arka.identityaccess.domain.service.PasswordPolicy;
import com.arka.identityaccess.domain.service.SessionPolicy;
import com.arka.identityaccess.domain.service.TokenPolicy;
import java.time.Instant;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LoginUseCase implements LoginCommandUseCase {

    private final LoginCommandAssembler loginCommandAssembler;
    private final SecurityRateLimitPort securityRateLimitPort;
    private final UserPersistencePort userPersistencePort;
    private final PasswordHashPort passwordHashPort;
    private final ClockPort clockPort;
    private final SessionPolicy sessionPolicy;
    private final TokenPolicy tokenPolicy;
    private final SessionPersistencePort sessionPersistencePort;
    private final JwtSigningPort jwtSigningPort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final LoginResultMapper loginResultMapper;
    private final PasswordPolicy passwordPolicy;

    public LoginUseCase(LoginCommandAssembler loginCommandAssembler, SecurityRateLimitPort securityRateLimitPort, UserPersistencePort userPersistencePort, PasswordHashPort passwordHashPort, ClockPort clockPort, SessionPolicy sessionPolicy, TokenPolicy tokenPolicy, SessionPersistencePort sessionPersistencePort, JwtSigningPort jwtSigningPort, SecurityAuditPort securityAuditPort, OutboxPersistencePort outboxPersistencePort, LoginResultMapper loginResultMapper, PasswordPolicy passwordPolicy) {
        this.loginCommandAssembler = loginCommandAssembler;
        this.securityRateLimitPort = securityRateLimitPort;
        this.userPersistencePort = userPersistencePort;
        this.passwordHashPort = passwordHashPort;
        this.clockPort = clockPort;
        this.sessionPolicy = sessionPolicy;
        this.tokenPolicy = tokenPolicy;
        this.sessionPersistencePort = sessionPersistencePort;
        this.jwtSigningPort = jwtSigningPort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.loginResultMapper = loginResultMapper;
        this.passwordPolicy = passwordPolicy;
    }

    @Override
    public Mono<LoginResult> handle(LoginCommand command) {
        passwordPolicy.ensureRawPasswordProvided(command.rawPassword());
        EmailAddress email = loginCommandAssembler.toEmailAddress(command);
        ClientDevice clientDevice = loginCommandAssembler.toClientDevice(command);
        ClientIp clientIp = loginCommandAssembler.toClientIp(command);

        return securityRateLimitPort
                .ensureLoginAllowed(email, clientIp)
                .then(Mono.defer(() -> userPersistencePort.loadForLogin(email)))
                .flatMap(user -> authenticateAndOpenSession(user, command.rawPassword(), clientDevice, clientIp))
                .flatMap(context -> sessionPersistencePort.create(context.session()).map(savedSession -> new MaterializedLoginContext(context.user(), savedSession)))
                .flatMap(context -> userPersistencePort.loadAuthorizationSnapshot(context.user().id())
                        .flatMap(snapshot -> Mono.zip(
                                        jwtSigningPort.signAccessToken(
                                                context.session(),
                                                snapshot.email(),
                                                snapshot.roles(),
                                                snapshot.permissions()),
                                        jwtSigningPort.signRefreshToken(context.session()))
                                .flatMap(tokens -> securityAuditPort.recordLoginSuccess(context.user(), context.session())
                                        .then(outboxPersistencePort.store(context.session().domainEvent()))
                                        .thenReturn(loginResultMapper.toResult(context.session(), tokens.getT1(), tokens.getT2())))));
    }

    private Mono<AuthenticatedLoginContext> authenticateAndOpenSession(UserAggregate user, String rawPassword, ClientDevice clientDevice, ClientIp clientIp) {
        return passwordHashPort.matches(rawPassword, user.credential().passwordHash())
                .map(passwordMatches -> {
                    Instant now = clockPort.now();
                    user.authenticate(passwordMatches, passwordPolicy, clientIp, now);
                    SessionAggregate session = SessionAggregate.open(user, clientDevice, clientIp, now, sessionPolicy, tokenPolicy);
                    return new AuthenticatedLoginContext(user, session);
                });
    }

    private record AuthenticatedLoginContext(UserAggregate user, SessionAggregate session) {}
    private record MaterializedLoginContext(UserAggregate user, SessionAggregate session) {}
}
