package com.sureshputla.ecommerce.order.outbox;

import com.sureshputla.ecommerce.order.model.OutboxEvent;
import com.sureshputla.ecommerce.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Transactional Outbox Pattern – relay component.
 *
 * <p>Polls the {@code outbox_events} table every 5 seconds for PENDING events,
 * publishes each one to its Kafka topic, then marks it as PUBLISHED.
 *
 * <p>At-least-once delivery guarantee: if the process crashes after publishing but
 * before the DB update, the event will be re-published on the next poll.
 * Consumers must be idempotent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxEventRepository.findByStatus(OutboxEvent.OutboxStatus.PENDING);

        if (pending.isEmpty()) {
            return;
        }

        log.debug("Outbox: found {} PENDING event(s) to publish", pending.size());

        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
                event.setStatus(OutboxEvent.OutboxStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                log.info("Published outbox event type={} aggregateId={}", event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}: {}", event.getId(), e.getMessage());
                event.setStatus(OutboxEvent.OutboxStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}

