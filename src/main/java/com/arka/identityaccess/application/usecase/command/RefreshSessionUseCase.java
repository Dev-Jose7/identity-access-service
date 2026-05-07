package com.arka.identityaccess.application.usecase.command;

import com.arka.identityaccess.application.command.RefreshSessionCommand;
import com.arka.identityaccess.application.mapper.command.RefreshSessionCommandAssembler;
import com.arka.identityaccess.application.mapper.result.TokenPairResultMapper;
import com.arka.identityaccess.application.port.in.RefreshSessionCommandUseCase;
import com.arka.identityaccess.application.port.out.audit.SecurityAuditPort;
import com.arka.identityaccess.application.port.out.cache.SecurityRateLimitPort;
import com.arka.identityaccess.application.port.out.external.ClockPort;
import com.arka.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.SessionPersistencePort;
import com.arka.identityaccess.application.port.out.persistence.UserPersistencePort;
import com.arka.identityaccess.application.port.out.security.JwtSigningPort;
import com.arka.identityaccess.application.result.TokenPairResult;
import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.service.SessionPolicy;
import com.arka.identityaccess.domain.service.TokenPolicy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RefreshSessionUseCase implements RefreshSessionCommandUseCase {

    private final SecurityRateLimitPort securityRateLimitPort;
    private final RefreshSessionCommandAssembler assembler;
    private final SessionPersistencePort sessionPersistencePort;
    private final ClockPort clockPort;
    private final SessionPolicy sessionPolicy;
    private final TokenPolicy tokenPolicy;
    private final JwtSigningPort jwtSigningPort;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final TokenPairResultMapper resultMapper;
    private final UserPersistencePort userPersistencePort;

    public RefreshSessionUseCase(
            SecurityRateLimitPort securityRateLimitPort,
            RefreshSessionCommandAssembler assembler,
            SessionPersistencePort sessionPersistencePort,
            ClockPort clockPort,
            SessionPolicy sessionPolicy,
            TokenPolicy tokenPolicy,
            JwtSigningPort jwtSigningPort,
            SecurityAuditPort securityAuditPort,
            OutboxPersistencePort outboxPersistencePort,
            TokenPairResultMapper resultMapper,
            UserPersistencePort userPersistencePort) {
        this.securityRateLimitPort = securityRateLimitPort;
        this.assembler = assembler;
        this.sessionPersistencePort = sessionPersistencePort;
        this.clockPort = clockPort;
        this.sessionPolicy = sessionPolicy;
        this.tokenPolicy = tokenPolicy;
        this.jwtSigningPort = jwtSigningPort;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.resultMapper = resultMapper;
        this.userPersistencePort = userPersistencePort;
    }

    @Override
    public Mono<TokenPairResult> handle(RefreshSessionCommand command) {
        ClientIp clientIp = ClientIp.of(command.ipAddress());
        return securityRateLimitPort
                .ensureRefreshAllowed(command.refreshToken(), clientIp)
                .then(Mono.fromSupplier(() -> assembler.toRefreshJti(command)))
                .flatMap(sessionPersistencePort::findActiveByRefreshJti)
                .map(session -> session.refresh(clockPort.now(), sessionPolicy, tokenPolicy))
                .flatMap(sessionPersistencePort::update)
                .flatMap(session -> userPersistencePort.loadAuthorizationSnapshot(session.userId())
                        .flatMap(snapshot -> Mono.zip(
                                        jwtSigningPort.signAccessToken(
                                                session,
                                                snapshot.email(),
                                                snapshot.roles(),
                                                snapshot.permissions()),
                                        jwtSigningPort.signRefreshToken(session))
                                .flatMap(tokens -> securityAuditPort.recordSessionRefreshed(session)
                                        .then(outboxPersistencePort.store(session.domainEvent()))
                                        .thenReturn(resultMapper.toResult(session, tokens.getT1(), tokens.getT2())))));
    }
}
