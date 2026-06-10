package com.draftlab.saga.messaging;

import com.draftlab.common.events.OrderCreatedEvent;
import com.draftlab.common.events.PaymentAuthorizedEvent;
import com.draftlab.common.events.PaymentFailedEvent;
import com.draftlab.common.events.Topics;
import com.draftlab.saga.domain.ProcessedMessage;
import com.draftlab.saga.repository.ProcessedMessageRepository;
import com.draftlab.saga.service.OrderSagaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventsListener {
    private static final String CONSUMER = "saga-orchestrator";
    private final OrderSagaService sagaService;
    private final ProcessedMessageRepository processedMessages;

    @KafkaListener(topics = Topics.ORDER_CREATED, groupId = "saga-orchestrator")
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        process(event.eventId(), event.correlationId(), () -> sagaService.start(event));
    }

    @KafkaListener(topics = Topics.PAYMENT_AUTHORIZED, groupId = "saga-orchestrator")
    @Transactional
    public void onPaymentAuthorized(PaymentAuthorizedEvent event) {
        process(event.eventId(), event.correlationId(), () -> sagaService.paymentAuthorized(event));
    }

    @KafkaListener(topics = Topics.PAYMENT_FAILED, groupId = "saga-orchestrator")
    @Transactional
    public void onPaymentFailed(PaymentFailedEvent event) {
        process(event.eventId(), event.correlationId(), () -> sagaService.paymentFailed(event));
    }

    private void process(java.util.UUID eventId, String correlationId, Runnable action) {
        try (var ignored = MDC.putCloseable("correlationId", correlationId)) {
            if (processedMessages.existsById(eventId)) {
                return;
            }
            action.run();
            processedMessages.save(new ProcessedMessage(eventId, CONSUMER));
        }
    }
}
