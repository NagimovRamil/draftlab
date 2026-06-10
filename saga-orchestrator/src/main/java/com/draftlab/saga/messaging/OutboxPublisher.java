package com.draftlab.saga.messaging;

import com.draftlab.saga.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${app.outbox-poll-delay:1000}")
    @Transactional
    public void publish() {
        for (var message : outboxRepository.findTop50ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            kafkaTemplate.send(message.getTopic(), message.getAggregateId(), message.getPayload()).join();
            message.setPublishedAt(Instant.now());
        }
    }
}
