package com.draftlab.ledger.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "ledger_reservations")
public class LedgerReservation {
    @Id
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Instant createdAt;

    public static LedgerReservation create(UUID orderId, BigDecimal amount, String currency) {
        var reservation = new LedgerReservation();
        reservation.id = UUID.randomUUID();
        reservation.orderId = orderId;
        reservation.amount = amount;
        reservation.currency = currency;
        reservation.status = "RESERVED";
        reservation.createdAt = Instant.now();
        return reservation;
    }
}
