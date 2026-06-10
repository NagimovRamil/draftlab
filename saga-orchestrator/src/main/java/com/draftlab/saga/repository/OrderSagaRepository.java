package com.draftlab.saga.repository;

import com.draftlab.saga.domain.OrderSaga;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSagaRepository extends JpaRepository<OrderSaga, UUID> {
    Optional<OrderSaga> findByOrderId(UUID orderId);
}
