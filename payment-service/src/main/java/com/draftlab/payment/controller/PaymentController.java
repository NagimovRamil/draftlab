package com.draftlab.payment.controller;

import com.draftlab.payment.domain.PaymentEntity;
import com.draftlab.payment.repository.PaymentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentRepository paymentRepository;

    @GetMapping("/by-order/{orderId}")
    public PaymentEntity byOrder(@PathVariable UUID orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow();
    }
}
