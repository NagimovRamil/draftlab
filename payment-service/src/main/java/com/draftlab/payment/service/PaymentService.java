package com.draftlab.payment.service;

import com.draftlab.common.events.AuthorizePaymentCommand;
import com.draftlab.common.events.PaymentAuthorizedEvent;
import com.draftlab.common.events.PaymentFailedEvent;
import com.draftlab.common.events.Topics;
import com.draftlab.payment.domain.OutboxMessage;
import com.draftlab.payment.domain.PaymentEntity;
import com.draftlab.payment.repository.OutboxRepository;
import com.draftlab.payment.repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final LedgerGateway ledgerGateway;
    private final ObjectMapper objectMapper;

    @Transactional
    public void authorize(AuthorizePaymentCommand event) {
        if (paymentRepository.findByOrderId(event.orderId()).isPresent()) {
            return;
        }
        try {
            ledgerGateway.reserve(new LedgerClient.LedgerReservationRequest(
                    event.orderId(), event.amount(), event.currency())).join();
            var payment = paymentRepository.save(PaymentEntity.authorized(event.orderId(), event.amount(), event.currency()));
            addOutbox(Topics.PAYMENT_AUTHORIZED, event.orderId(),
                    new PaymentAuthorizedEvent(UUID.randomUUID(), event.orderId(), payment.getId(),
                            event.amount(), event.currency(), Instant.now(), event.correlationId()));
        } catch (RuntimeException ex) {
            paymentRepository.save(PaymentEntity.failed(event.orderId(), event.amount(), event.currency(), "Ledger is unavailable"));
            addOutbox(Topics.PAYMENT_FAILED, event.orderId(),
                    new PaymentFailedEvent(UUID.randomUUID(), event.orderId(), "Ledger is unavailable",
                            Instant.now(), event.correlationId()));
        }
    }

    private void addOutbox(String topic, UUID aggregateId, Object event) {
        outboxRepository.save(new OutboxMessage(extractEventId(event), topic, aggregateId.toString(),
                event.getClass().getSimpleName(), write(event)));
    }

    private UUID extractEventId(Object event) {
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
