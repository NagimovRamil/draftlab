package com.draftlab.order.service;

import com.draftlab.order.domain.OrderStatus;
import com.draftlab.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;

    @Cacheable(cacheNames = "orders", key = "#id")
    public OrderView get(UUID id) {
        var order = orderRepository.findById(id).orElseThrow();
        return new OrderView(order.getId(), order.getCustomerId(), order.getAmount(), order.getCurrency(),
                order.getStatus(), order.getCreatedAt(), order.getUpdatedAt());
    }

    public record OrderView(
            UUID id,
            String customerId,
            BigDecimal amount,
            String currency,
            OrderStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
