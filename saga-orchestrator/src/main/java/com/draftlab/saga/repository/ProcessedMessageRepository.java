package com.draftlab.saga.repository;

import com.draftlab.saga.domain.ProcessedMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {
}
