package com.arka.identityaccess.infrastructure.config;

import com.arka.identityaccess.application.service.OutboxEventRelayPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private final OutboxEventRelayPublisher outboxEventRelayPublisher;

    @Value("${app.outbox.relay.enabled:true}")
    private boolean enabled;

    @Value("${app.outbox.relay.batch-size:200}")
    private int batchSize;

    public OutboxRelayScheduler(OutboxEventRelayPublisher outboxEventRelayPublisher) {
        this.outboxEventRelayPublisher = outboxEventRelayPublisher;
    }

    @Scheduled(fixedDelayString = "${app.outbox.relay.poll-interval-ms:2000}")
    public void publishPendingEvents() {
        if (!enabled) {
            return;
        }
        outboxEventRelayPublisher
                .publishPending(batchSize)
                .doOnError(throwable -> log.error(
                        "Outbox relay scheduler execution failed: batchSize={}, error={}",
                        batchSize,
                        throwable.getMessage(),
                        throwable))
                .onErrorResume(throwable -> Mono.empty())
                .subscribe();
    }
}
