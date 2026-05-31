package com.draftlab.order.repository;

import com.draftlab.order.domain.ProcessedMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {
}
