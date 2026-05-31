package com.draftlab.order.messaging;

import com.draftlab.common.events.PaymentAuthorizedEvent;
import com.draftlab.common.events.PaymentFailedEvent;
import com.draftlab.common.events.Topics;
import com.draftlab.order.domain.OrderStatus;
import com.draftlab.order.domain.ProcessedMessage;
import com.draftlab.order.repository.OrderRepository;
import com.draftlab.order.repository.ProcessedMessageRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventsListener {
    private static final String CONSUMER = "order-service-payment-events";
    private final OrderRepository orderRepository;
    private final ProcessedMessageRepository processedMessages;
    private final CacheManager cacheManager;

    @KafkaListener(topics = Topics.PAYMENT_AUTHORIZED, groupId = "order-service")
    @Transactional
    public void onAuthorized(PaymentAuthorizedEvent event) {
        if (processedMessages.existsById(event.eventId())) {
            return;
        }
        var order = orderRepository.findById(event.orderId()).orElseThrow();
        order.setStatus(OrderStatus.PAYMENT_AUTHORIZED);
        order.setUpdatedAt(Instant.now());
        processedMessages.save(new ProcessedMessage(event.eventId(), CONSUMER));
        evictOrder(event.orderId());
    }

    @KafkaListener(topics = Topics.PAYMENT_FAILED, groupId = "order-service")
    @Transactional
    public void onFailed(PaymentFailedEvent event) {
        if (processedMessages.existsById(event.eventId())) {
            return;
        }
        var order = orderRepository.findById(event.orderId()).orElseThrow();
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        order.setUpdatedAt(Instant.now());
        processedMessages.save(new ProcessedMessage(event.eventId(), CONSUMER));
        evictOrder(event.orderId());
    }

    private void evictOrder(UUID orderId) {
        var cache = cacheManager.getCache("orders");
        if (cache != null) {
            cache.evict(orderId);
        }
    }
}
