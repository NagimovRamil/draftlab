package com.draftlab.payment.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LedgerGateway {
    private final LedgerClient ledgerClient;
    @Qualifier("ledgerExecutor")
    private final Executor ledgerExecutor;

    @Retry(name = "ledger")
    @CircuitBreaker(name = "ledger")
    @Bulkhead(name = "ledger", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "ledger")
    public CompletableFuture<LedgerClient.LedgerReservationResponse> reserve(
            LedgerClient.LedgerReservationRequest request) {
        return CompletableFuture.supplyAsync(() -> ledgerClient.reserve(request), ledgerExecutor);
    }
}
