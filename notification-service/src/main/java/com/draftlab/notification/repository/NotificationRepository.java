package com.draftlab.notification.repository;

import com.draftlab.notification.domain.NotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByOrderId(UUID orderId);
}
