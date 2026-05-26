package com.sureshputla.ecommerce.payment.outbox;

import com.sureshputla.ecommerce.payment.model.OutboxEvent;
import com.sureshputla.ecommerce.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox relay for Payment Service.
 * Polls every 5 seconds and forwards PENDING events to their Kafka topics.
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

        for (OutboxEvent event : pending) {
            try {
                kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload());
                event.setStatus(OutboxEvent.OutboxStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(event);
                log.info("Payment outbox published type={} aggregateId={}", event.getEventType(), event.getAggregateId());
            } catch (Exception e) {
                log.error("Payment outbox failed to publish event id={}: {}", event.getId(), e.getMessage());
                event.setStatus(OutboxEvent.OutboxStatus.FAILED);
                outboxEventRepository.save(event);
            }
        }
    }
}

