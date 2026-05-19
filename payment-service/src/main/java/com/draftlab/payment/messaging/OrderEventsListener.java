package com.draftlab.payment.messaging;

import com.draftlab.common.events.OrderCreatedEvent;
import com.draftlab.common.events.Topics;
import com.draftlab.payment.domain.ProcessedMessage;
import com.draftlab.payment.repository.ProcessedMessageRepository;
import com.draftlab.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventsListener {
    private static final String CONSUMER = "payment-service-order-events";
    private final PaymentService paymentService;
    private final ProcessedMessageRepository processedMessages;

    @KafkaListener(topics = Topics.ORDER_CREATED, groupId = "payment-service")
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        if (processedMessages.existsById(event.eventId())) {
            return;
        }
        paymentService.authorize(event);
        processedMessages.save(new ProcessedMessage(event.eventId(), CONSUMER));
    }
}
