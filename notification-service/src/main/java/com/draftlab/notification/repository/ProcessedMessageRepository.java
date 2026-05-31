package com.draftlab.notification.repository;

import com.draftlab.notification.domain.ProcessedMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {
}
