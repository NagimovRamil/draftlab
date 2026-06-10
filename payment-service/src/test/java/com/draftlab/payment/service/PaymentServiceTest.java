package com.draftlab.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.draftlab.common.events.AuthorizePaymentCommand;
import com.draftlab.common.events.Topics;
import com.draftlab.payment.domain.OutboxMessage;
import com.draftlab.payment.domain.PaymentEntity;
import com.draftlab.payment.repository.OutboxRepository;
import com.draftlab.payment.repository.PaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private LedgerGateway ledgerGateway;

    @Test
    void publishesAuthorizedEventAfterLedgerReservation() {
        var command = command();
        var payment = PaymentEntity.authorized(command.orderId(), command.amount(), command.currency());
        when(paymentRepository.findByOrderId(command.orderId())).thenReturn(Optional.empty());
        when(ledgerGateway.reserve(any())).thenReturn(CompletableFuture.completedFuture(
                new LedgerClient.LedgerReservationResponse(UUID.randomUUID(), "RESERVED")));
        when(paymentRepository.save(any(PaymentEntity.class))).thenReturn(payment);

        service().authorize(command);

        assertPublishedTopic(Topics.PAYMENT_AUTHORIZED);
    }

    @Test
    void publishesFailedEventWhenLedgerCallFails() {
        var command = command();
        when(paymentRepository.findByOrderId(command.orderId())).thenReturn(Optional.empty());
        when(ledgerGateway.reserve(any())).thenReturn(CompletableFuture.failedFuture(
                new IllegalStateException("ledger unavailable")));
        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service().authorize(command);

        assertPublishedTopic(Topics.PAYMENT_FAILED);
    }

    private PaymentService service() {
        var mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return new PaymentService(paymentRepository, outboxRepository, ledgerGateway, mapper);
    }

    private AuthorizePaymentCommand command() {
        return new AuthorizePaymentCommand(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "customer-1",
                new BigDecimal("12.50"), "USD", Instant.now(), "corr-1");
    }

    private void assertPublishedTopic(String topic) {
        var outbox = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepository).save(outbox.capture());
        assertThat(outbox.getValue().getTopic()).isEqualTo(topic);
        assertThat(outbox.getValue().getPayload()).contains("\"correlationId\":\"corr-1\"");
    }
}
