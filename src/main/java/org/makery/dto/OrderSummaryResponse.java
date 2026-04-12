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
        LocalDateTime pickupDate,
        LocalDateTime createdAt
) {
    public static OrderSummaryResponse from(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStore().getName(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getPickupDate(),
                order.getCreatedAt()
        );
    }
}
