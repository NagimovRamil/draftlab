package com.draftlab.common.events;

import java.time.Instant;
import java.util.UUID;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record NotificationRequestedEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        String channel,
        String message,
        Instant occurredAt
) {
}
