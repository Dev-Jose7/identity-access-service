package io.identityaccess.infrastructure.config;

import io.identityaccess.application.port.out.external.ClockPort;
import io.identityaccess.application.port.out.persistence.SessionPersistencePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SessionExpirationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionExpirationScheduler.class);

    private final SessionPersistencePort sessionPersistencePort;
    private final ClockPort clockPort;

    @Value("${app.sessions.expiration.enabled:true}")
    private boolean enabled;

    public SessionExpirationScheduler(SessionPersistencePort sessionPersistencePort, ClockPort clockPort) {
        this.sessionPersistencePort = sessionPersistencePort;
        this.clockPort = clockPort;
    }

    @Scheduled(fixedDelayString = "${app.sessions.expiration.sweep-interval-ms:60000}")
    public void expireRefreshExpiredSessions() {
        if (!enabled) {
            return;
        }
        sessionPersistencePort
                .expireExpiredSessions(clockPort.now())
                .doOnNext(expiredSessions -> {
                    if (expiredSessions > 0) {
                        LOGGER.info("Expired {} session(s) whose refresh token lifetime ended", expiredSessions);
                    }
                })
                .onErrorResume(throwable -> {
                    LOGGER.warn("Session expiration sweep failed", throwable);
                    return Mono.empty();
                })
                .subscribe();
    }
}
