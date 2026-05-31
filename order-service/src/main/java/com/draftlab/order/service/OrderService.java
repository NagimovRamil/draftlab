package com.draftlab.order.service;

import com.draftlab.common.events.OrderCreatedEvent;
import com.draftlab.common.events.Topics;
import com.draftlab.order.domain.OrderEntity;
import com.draftlab.order.domain.OutboxMessage;
import com.draftlab.order.repository.OrderRepository;
import com.draftlab.order.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderEntity create(String customerId, BigDecimal amount, String currency) {
        var order = orderRepository.save(OrderEntity.create(customerId, amount, currency));
        var event = new OrderCreatedEvent(UUID.randomUUID(), order.getId(), customerId, amount, currency, Instant.now());
        outboxRepository.save(new OutboxMessage(event.eventId(), Topics.ORDER_CREATED, order.getId().toString(),
                event.getClass().getSimpleName(), write(event)));
        return order;
    }

    public OrderEntity get(UUID id) {
        return orderRepository.findById(id).orElseThrow();
    }

    private String write(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize outbox payload", ex);
        }
    }
}
