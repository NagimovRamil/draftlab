package com.draftlab.saga.domain;

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
@Table(name = "outbox_messages")
public class OutboxMessage {
    @Id
    @NonNull
    private UUID id;
    @NonNull
    private String topic;
    @NonNull
    private String aggregateId;
    @NonNull
    private String type;
    @NonNull
    private String payload;
    private Instant createdAt = Instant.now();
    private Instant publishedAt;
}
