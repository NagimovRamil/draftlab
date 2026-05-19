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
@Table(name = "processed_messages")
public class ProcessedMessage {
    @Id
    @NonNull
    private UUID eventId;
    @NonNull
    private String consumerName;
    private Instant processedAt = Instant.now();
}
