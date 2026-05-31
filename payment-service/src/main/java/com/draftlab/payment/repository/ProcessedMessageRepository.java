package com.draftlab.payment.repository;

import com.draftlab.payment.domain.ProcessedMessage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, UUID> {
}
