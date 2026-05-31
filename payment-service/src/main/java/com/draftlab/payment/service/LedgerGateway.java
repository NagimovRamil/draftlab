package com.draftlab.payment.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LedgerGateway {
    private final LedgerClient ledgerClient;

    @Retry(name = "ledger")
    @CircuitBreaker(name = "ledger")
    @Bulkhead(name = "ledger", type = Bulkhead.Type.SEMAPHORE)
    public LedgerClient.LedgerReservationResponse reserve(LedgerClient.LedgerReservationRequest request) {
        return ledgerClient.reserve(request);
    }
}
