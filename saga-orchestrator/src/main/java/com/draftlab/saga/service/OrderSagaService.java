package com.draftlab.saga.service;

import com.draftlab.common.events.AuthorizePaymentCommand;
import com.draftlab.common.events.NotificationRequestedEvent;
import com.draftlab.common.events.OrderCreatedEvent;
import com.draftlab.common.events.PaymentAuthorizedEvent;
import com.draftlab.common.events.PaymentFailedEvent;
import com.draftlab.common.events.Topics;
import com.draftlab.saga.domain.OrderSaga;
import com.draftlab.saga.domain.OutboxMessage;
import com.draftlab.saga.repository.OrderSagaRepository;
import com.draftlab.saga.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSagaService {
    private final OrderSagaRepository sagaRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void start(OrderCreatedEvent event) {
        if (sagaRepository.findByOrderId(event.orderId()).isPresent()) {
            return;
        }
        var saga = sagaRepository.save(OrderSaga.start(event.orderId(), event.customerId()));
        publish(Topics.AUTHORIZE_PAYMENT, event.orderId(), new AuthorizePaymentCommand(
                UUID.randomUUID(), saga.getId(), event.orderId(), event.customerId(),
                event.amount(), event.currency(), Instant.now(), event.correlationId()));
    }

    @Transactional
    public void paymentAuthorized(PaymentAuthorizedEvent event) {
        var saga = sagaRepository.findByOrderId(event.orderId()).orElseThrow();
        if (saga.getStatus() != com.draftlab.saga.domain.OrderSagaStatus.PAYMENT_PENDING) {
            return;
        }
        publish(Topics.NOTIFICATION_REQUESTED, event.orderId(), new NotificationRequestedEvent(
                UUID.randomUUID(), event.orderId(), saga.getCustomerId(), "EMAIL",
                "Payment authorized for order " + event.orderId(), Instant.now(), event.correlationId()));
        saga.complete();
    }

    @Transactional
    public void paymentFailed(PaymentFailedEvent event) {
        var saga = sagaRepository.findByOrderId(event.orderId()).orElseThrow();
        if (saga.getStatus() == com.draftlab.saga.domain.OrderSagaStatus.PAYMENT_PENDING) {
            saga.fail(event.reason());
        }
    }

    private void publish(String topic, UUID aggregateId, Object event) {
        outboxRepository.save(new OutboxMessage(eventId(event), topic, aggregateId.toString(),
                event.getClass().getSimpleName(), write(event)));
    }

    private UUID eventId(Object event) {
        try {
            return (UUID) event.getClass().getMethod("eventId").invoke(event);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalArgumentException("Event must expose eventId()", ex);
        }
    }

    private String write(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize outbox payload", ex);
        }
    }
}
