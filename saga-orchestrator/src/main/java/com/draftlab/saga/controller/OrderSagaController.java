package com.draftlab.saga.controller;

import com.draftlab.saga.domain.OrderSaga;
import com.draftlab.saga.repository.OrderSagaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sagas")
@RequiredArgsConstructor
public class OrderSagaController {
    private final OrderSagaRepository repository;

    @GetMapping("/by-order/{orderId}")
    public OrderSaga byOrder(@PathVariable UUID orderId) {
        return repository.findByOrderId(orderId).orElseThrow();
    }
}
