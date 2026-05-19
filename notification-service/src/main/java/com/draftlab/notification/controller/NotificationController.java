package com.draftlab.notification.controller;

import com.draftlab.notification.domain.NotificationEntity;
import com.draftlab.notification.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;

    @GetMapping("/by-order/{orderId}")
    public List<NotificationEntity> byOrder(@PathVariable UUID orderId) {
        return notificationRepository.findByOrderId(orderId);
    }
}
