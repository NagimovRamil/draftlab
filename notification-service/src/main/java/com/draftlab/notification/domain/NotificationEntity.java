package com.draftlab.notification.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    private UUID id = UUID.randomUUID();
    @NonNull
    private UUID orderId;
    @NonNull
    private String customerId;
    @NonNull
    private String channel;
    @NonNull
    private String message;
    private Instant createdAt = Instant.now();
}
