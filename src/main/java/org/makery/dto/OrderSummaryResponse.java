package org.makery.dto;

import org.makery.domain.Order;
import org.makery.domain.OrderStatus;

import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        String orderNumber,
        String storeName,
        OrderStatus status,
        int totalPrice,
        Integer extraFee,
        String extraFeeReason,
        LocalDateTime pickupDate,
        LocalDateTime createdAt,
        boolean hasReview
) {
    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStore().getName(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getExtraFee() != null ? order.getExtraFee() : 0,
                order.getExtraFeeReason(),
                order.getPickupDate(),
                order.getCreatedAt(),
                order.getReview() != null
        );
    }
}
