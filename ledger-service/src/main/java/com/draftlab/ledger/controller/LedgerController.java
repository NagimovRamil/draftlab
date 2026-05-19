package com.draftlab.ledger.controller;

import com.draftlab.ledger.service.LedgerService;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ledger/reservations")
@RequiredArgsConstructor
public class LedgerController {
    private final LedgerService ledgerService;
    @Value("${app.response-delay:0}")
    private Duration responseDelay;

    @PostMapping
    public LedgerReservationResponse reserve(@RequestBody LedgerReservationRequest request) throws InterruptedException {
        if (!responseDelay.isZero()) {
            Thread.sleep(responseDelay.toMillis());
        }
        var reservation = ledgerService.reserve(request.orderId(), request.amount(), request.currency());
        return new LedgerReservationResponse(reservation.getId(), reservation.getStatus());
    }

    public record LedgerReservationRequest(UUID orderId, BigDecimal amount, String currency) {
    }

    public record LedgerReservationResponse(UUID reservationId, String status) {
    }
}
