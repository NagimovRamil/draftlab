package com.draftlab.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record PaymentAuthorizedEvent(
        UUID eventId,
        UUID orderId,
        UUID paymentId,
        BigDecimal amount,
        String currency,
        Instant occurredAt,
        String correlationId
) {
}
