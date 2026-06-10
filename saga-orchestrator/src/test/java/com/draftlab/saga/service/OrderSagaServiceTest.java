package com.draftlab.saga.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.draftlab.common.events.AuthorizePaymentCommand;
import com.draftlab.common.events.OrderCreatedEvent;
import com.draftlab.common.events.Topics;
import com.draftlab.saga.domain.OrderSaga;
import com.draftlab.saga.domain.OutboxMessage;
import com.draftlab.saga.repository.OrderSagaRepository;
import com.draftlab.saga.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderSagaServiceTest {
    @Mock
    private OrderSagaRepository sagaRepository;
    @Mock
    private OutboxRepository outboxRepository;

    @Test
    void startsSagaAndPublishesAuthorizePaymentCommand() throws Exception {
        var mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var service = new OrderSagaService(sagaRepository, outboxRepository, mapper);
        var orderId = UUID.randomUUID();
        var event = new OrderCreatedEvent(UUID.randomUUID(), orderId, "customer-1",
                new BigDecimal("12.50"), "USD", Instant.now(), "corr-1");
        when(sagaRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(sagaRepository.save(any(OrderSaga.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.start(event);

        verify(sagaRepository).save(any(OrderSaga.class));
        var outbox = ArgumentCaptor.forClass(OutboxMessage.class);
        verify(outboxRepository).save(outbox.capture());
        assertThat(outbox.getValue().getTopic()).isEqualTo(Topics.AUTHORIZE_PAYMENT);
        var command = mapper.readValue(outbox.getValue().getPayload(), AuthorizePaymentCommand.class);
        assertThat(command.orderId()).isEqualTo(orderId);
        assertThat(command.correlationId()).isEqualTo("corr-1");
    }
}
