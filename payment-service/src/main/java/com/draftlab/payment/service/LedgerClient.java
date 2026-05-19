package com.draftlab.payment.service;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ledger-service", url = "${app.ledger-url:http://localhost:8083}")
public interface LedgerClient {
    @PostMapping("/api/ledger/reservations")
    LedgerReservationResponse reserve(@RequestBody LedgerReservationRequest request);

    record LedgerReservationRequest(UUID orderId, BigDecimal amount, String currency) {
    }

    record LedgerReservationResponse(UUID reservationId, String status) {
    }
}
