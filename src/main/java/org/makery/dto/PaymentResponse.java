package org.makery.dto;

import org.makery.domain.Payment;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        String orderNumber,
        Integer amount,
        String methodType,
        String pgTid,
        String status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getOrderNumber(),
                payment.getAmount(),
                payment.getMethodType() != null ? payment.getMethodType().name() : null,
                payment.getPgTid(),
                payment.getStatus() != null ? payment.getStatus().name() : null,
                payment.getCreatedAt(),
                payment.getModifiedAt()
        );
    }
}