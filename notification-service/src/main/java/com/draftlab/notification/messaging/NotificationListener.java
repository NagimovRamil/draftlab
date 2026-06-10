package com.draftlab.notification.messaging;

import com.draftlab.common.events.NotificationRequestedEvent;
import com.draftlab.common.events.Topics;
import com.draftlab.notification.domain.NotificationEntity;
import com.draftlab.notification.domain.ProcessedMessage;
import com.draftlab.notification.repository.NotificationRepository;
import com.draftlab.notification.repository.ProcessedMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {
    private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);
    private static final String CONSUMER = "notification-service-notification-events";
    private final NotificationRepository notificationRepository;
    private final ProcessedMessageRepository processedMessages;

    @KafkaListener(topics = Topics.NOTIFICATION_REQUESTED, groupId = "notification-service")
    @Transactional
    public void onNotificationRequested(NotificationRequestedEvent event) {
        try (var ignored = MDC.putCloseable("correlationId", event.correlationId())) {
            if (processedMessages.existsById(event.eventId())) {
                return;
            }
            var notification = notificationRepository.save(new NotificationEntity(
                    event.orderId(), event.customerId(), event.channel(), event.message()));
            processedMessages.save(new ProcessedMessage(event.eventId(), CONSUMER));
            log.info("Notification {} stored for order {}", notification.getId(), event.orderId());
        }
    }
}
