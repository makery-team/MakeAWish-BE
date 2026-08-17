package org.makery.dto;

public record TossPaymentConfirmRequest(
        String paymentKey,
        String orderNumber,
        Integer amount
) {}
