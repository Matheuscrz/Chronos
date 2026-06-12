package com.caelum.chronos.shared.infra.messaging.outbox;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.caelum.chronos.shared.infra.config.messaging.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private static final int MAX_RETRIES = 5;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxEvent.OutboxStatus.PENDING);
        
        if (events.isEmpty()) return;
        
        log.debug("Processing {} outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                String exchange = getExchangeFor(event.getAggregateType());
                String routingKey = getRoutingKeyFor(event.getEventType());

                if (exchange != null && routingKey != null) {
                    rabbitTemplate.convertAndSend(exchange, routingKey, event.getPayload());
                    event.setStatus(OutboxEvent.OutboxStatus.PROCESSED);
                    event.setProcessedAt(java.time.Instant.now());
                } else {
                    log.warn("No exchange/routingKey mapping for event: {}", event.getEventType());
                    event.setStatus(OutboxEvent.OutboxStatus.FAILED);
                    event.setErrorMessage("No mapping found");
                }

            } catch (Exception e) {
                log.error("Failed to process outbox event: {}. Retry count: {}", event.getId(), event.getRetryCount(), e);
                event.incrementRetryCount();
                if (event.getRetryCount() >= MAX_RETRIES) {
                    event.setStatus(OutboxEvent.OutboxStatus.FAILED);
                }
                event.setErrorMessage(e.getMessage());
            }
        }
        
        outboxRepository.saveAll(events);
    }

    private String getExchangeFor(String aggregateType) {
        if ("WorkOrder".equals(aggregateType)) return RabbitMQConfig.WORK_ORDER_EXCHANGE;
        return null;
    }

    private String getRoutingKeyFor(String eventType) {
        if ("WorkOrderCompleted".equals(eventType)) return RabbitMQConfig.WORK_ORDER_COMPLETED_ROUTING_KEY;
        return null;
    }
}
