package com.draftlab.payment.domain;

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
@Table(name = "payments")
public class PaymentEntity {
    @Id
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private String failureReason;
    private Instant createdAt;

    public static PaymentEntity authorized(UUID orderId, BigDecimal amount, String currency) {
        var payment = base(orderId, amount, currency);
        payment.status = PaymentStatus.AUTHORIZED;
        return payment;
    }

    public static PaymentEntity failed(UUID orderId, BigDecimal amount, String currency, String reason) {
        var payment = base(orderId, amount, currency);
        payment.status = PaymentStatus.FAILED;
        payment.failureReason = reason;
        return payment;
    }

    private static PaymentEntity base(UUID orderId, BigDecimal amount, String currency) {
        var payment = new PaymentEntity();
        payment.id = UUID.randomUUID();
        payment.orderId = orderId;
        payment.amount = amount;
        payment.currency = currency;
        payment.createdAt = Instant.now();
        return payment;
    }
}
