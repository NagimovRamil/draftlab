package com.draftlab.ledger.service;

import com.draftlab.ledger.domain.LedgerReservation;
import com.draftlab.ledger.repository.LedgerReservationRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LedgerService {
    private final LedgerReservationRepository repository;

    @Transactional
    public LedgerReservation reserve(UUID orderId, BigDecimal amount, String currency) {
        return repository.findByOrderId(orderId)
                .orElseGet(() -> repository.save(LedgerReservation.create(orderId, amount, currency)));
    }
}
