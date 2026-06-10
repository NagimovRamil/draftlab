package com.draftlab.saga.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "order_sagas")
public class OrderSaga {
    @Id
    private UUID id;
    private UUID orderId;
    private String customerId;
    @Enumerated(EnumType.STRING)
    private OrderSagaStatus status;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderSaga start(UUID orderId, String customerId) {
        var now = Instant.now();
        var saga = new OrderSaga();
        saga.id = UUID.randomUUID();
        saga.orderId = orderId;
        saga.customerId = customerId;
        saga.status = OrderSagaStatus.PAYMENT_PENDING;
        saga.createdAt = now;
        saga.updatedAt = now;
        return saga;
    }

    public void complete() {
        status = OrderSagaStatus.COMPLETED;
        updatedAt = Instant.now();
    }

    public void fail(String reason) {
        status = OrderSagaStatus.FAILED;
        failureReason = reason;
        updatedAt = Instant.now();
    }
}
