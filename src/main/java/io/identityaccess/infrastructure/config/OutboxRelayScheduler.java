package io.identityaccess.infrastructure.config;

import io.identityaccess.application.service.OutboxEventRelayPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class OutboxRelayScheduler {

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
                .onErrorResume(throwable -> Mono.empty())
                .subscribe();
    }
}
