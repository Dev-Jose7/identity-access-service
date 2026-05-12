package io.identityaccess.application.usecase.command;

import io.identityaccess.application.command.LogoutCommand;
import io.identityaccess.application.mapper.command.LogoutCommandAssembler;
import io.identityaccess.application.mapper.result.LogoutResultMapper;
import io.identityaccess.application.port.in.LogoutCommandUseCase;
import io.identityaccess.application.port.out.audit.SecurityAuditPort;
import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.OutboxPersistencePort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import io.identityaccess.application.result.LogoutResult;
import io.identityaccess.domain.service.SessionPolicy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LogoutUseCase implements LogoutCommandUseCase {

    private final LogoutCommandAssembler assembler;
    private final SessionPersistencePort sessionPersistencePort;
    private final ClockPort clockPort;
    private final SessionPolicy sessionPolicy;
    private final SecurityAuditPort securityAuditPort;
    private final OutboxPersistencePort outboxPersistencePort;
    private final LogoutResultMapper resultMapper;

    public LogoutUseCase(LogoutCommandAssembler assembler, SessionPersistencePort sessionPersistencePort, ClockPort clockPort, SessionPolicy sessionPolicy, SecurityAuditPort securityAuditPort, OutboxPersistencePort outboxPersistencePort, LogoutResultMapper resultMapper) {
        this.assembler = assembler;
        this.sessionPersistencePort = sessionPersistencePort;
        this.clockPort = clockPort;
        this.sessionPolicy = sessionPolicy;
        this.securityAuditPort = securityAuditPort;
        this.outboxPersistencePort = outboxPersistencePort;
        this.resultMapper = resultMapper;
    }

    @Override
    public Mono<LogoutResult> handle(LogoutCommand command) {
        return sessionPersistencePort.findBySessionId(assembler.toSessionId(command))
                .map(session -> session.revoke(clockPort.now(), sessionPolicy))
                .flatMap(sessionPersistencePort::update)
                .flatMap(session -> securityAuditPort.recordSessionRevoked(session)
                        .then(outboxPersistencePort.store(session.domainEvent()))
                        .thenReturn(resultMapper.toResult(session)));
    }
}
