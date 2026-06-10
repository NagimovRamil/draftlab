package com.draftlab.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record AuthorizePaymentCommand(
        UUID eventId,
        UUID sagaId,
        UUID orderId,
        String customerId,
        BigDecimal amount,
        String currency,
        Instant occurredAt,
        String correlationId
) {
}
