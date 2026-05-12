package io.identityaccess.infrastructure.config;

import io.identityaccess.application.service.OutboxEventRelayPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxRelayScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxRelayScheduler.class);

    private final OutboxEventRelayPublisher outboxEventRelayPublisher;

    @Value("${app.outbox.relay.enabled:true}")
    private boolean enabled;

    @Value("${app.outbox.relay.batch-size:200}")
    private int batchSize;

    @Value("${app.outbox.relay.max-retries:3}")
    private int maxRetries;

    public OutboxRelayScheduler(OutboxEventRelayPublisher outboxEventRelayPublisher) {
        this.outboxEventRelayPublisher = outboxEventRelayPublisher;
    }

    @Scheduled(fixedDelayString = "${app.outbox.relay.poll-interval-ms:2000}")
    public void publishPendingEvents() {
        if (!enabled) {
            return;
        }
        outboxEventRelayPublisher
                .publishPending(batchSize, maxRetries)
                .subscribe(
                        ignored -> { },
                        throwable -> LOGGER.error(
                                "Outbox relay batch failed batchSize={} maxRetries={} error={}",
                                batchSize,
                                maxRetries,
                                throwable.getMessage(),
                                throwable));
    }
}
