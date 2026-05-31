package com.draftlab.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "orders")
public class OrderEntity {
    @Id
    private UUID id;
    private String customerId;
    private BigDecimal amount;
    private String currency;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderEntity create(String customerId, BigDecimal amount, String currency) {
        var now = Instant.now();
        var order = new OrderEntity();
        order.id = UUID.randomUUID();
        order.customerId = customerId;
        order.amount = amount;
        order.currency = currency;
        order.status = OrderStatus.NEW;
        order.createdAt = now;
        order.updatedAt = now;
        return order;
    }
}
