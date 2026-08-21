package org.makery.dto;

import org.makery.domain.Order;
import org.makery.domain.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record OrderDetailResponse(
        Long id,
        String orderNumber,
        String storeName,
        OrderStatus status,
        LocalDateTime pickupDate,
        int totalPrice,
        Integer extraFee,
        String extraFeeReason,
        Map<String, Object> orderData,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        boolean hasReview
) {
    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStore().getName(),
                order.getStatus(),
                order.getPickupDate(),
                order.getTotalPrice(),
                order.getExtraFee() != null ? order.getExtraFee() : 0,
                order.getExtraFeeReason(),
                order.getOrderData(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getCreatedAt(),
                order.getReview() != null
        );
    }
}