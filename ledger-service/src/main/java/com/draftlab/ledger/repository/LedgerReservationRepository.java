package com.draftlab.ledger.repository;

import com.draftlab.ledger.domain.LedgerReservation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerReservationRepository extends JpaRepository<LedgerReservation, UUID> {
    Optional<LedgerReservation> findByOrderId(UUID orderId);
}
