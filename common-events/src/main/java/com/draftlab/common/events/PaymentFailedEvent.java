package com.draftlab.common.events;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record PaymentFailedEvent(
        UUID eventId,
        UUID orderId,
        String reason,
        Instant occurredAt,
        String correlationId
) {
}
