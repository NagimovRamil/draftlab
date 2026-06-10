package com.draftlab.saga.repository;

import com.draftlab.saga.domain.OutboxMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {
    List<OutboxMessage> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
}
