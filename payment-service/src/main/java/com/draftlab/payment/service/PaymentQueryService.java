package com.draftlab.payment.service;

import com.draftlab.payment.domain.PaymentEntity;
import com.draftlab.payment.repository.PaymentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {
    private final PaymentRepository paymentRepository;

    @Cacheable(cacheNames = "payments-by-order", key = "#orderId")
    public PaymentEntity findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow();
    }
}
