package com.draftlab.payment.messaging;

import com.draftlab.common.events.AuthorizePaymentCommand;
import com.draftlab.common.events.Topics;
import com.draftlab.payment.domain.ProcessedMessage;
import com.draftlab.payment.repository.ProcessedMessageRepository;
import com.draftlab.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventsListener {
    private static final String CONSUMER = "payment-service-payment-commands";
    private final PaymentService paymentService;
    private final ProcessedMessageRepository processedMessages;

    @KafkaListener(topics = Topics.AUTHORIZE_PAYMENT, groupId = "payment-service")
    @Transactional
    public void onAuthorizePayment(AuthorizePaymentCommand event) {
        try (var ignored = MDC.putCloseable("correlationId", event.correlationId())) {
            if (processedMessages.existsById(event.eventId())) {
                return;
            }
            paymentService.authorize(event);
            processedMessages.save(new ProcessedMessage(event.eventId(), CONSUMER));
        }
    }
}
