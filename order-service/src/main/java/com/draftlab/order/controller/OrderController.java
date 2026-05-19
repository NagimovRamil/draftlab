package com.draftlab.order.controller;

import com.draftlab.order.domain.OrderEntity;
import com.draftlab.order.service.OrderService;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderEntity create(@RequestBody CreateOrderRequest request) {
        return orderService.create(request.customerId(), request.amount(), request.currency());
    }

    @GetMapping("/{id}")
    public OrderEntity get(@PathVariable UUID id) {
        return orderService.get(id);
    }

    public record CreateOrderRequest(String customerId, BigDecimal amount, String currency) {
    }
}
