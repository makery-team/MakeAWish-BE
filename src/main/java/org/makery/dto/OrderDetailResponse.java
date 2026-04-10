package org.makery.dto;

import org.makery.domain.Order;
import org.makery.domain.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        String orderNumber,
        String storeName,
        OrderStatus status,
        LocalDate pickupDate,
        int totalPrice,
        String orderData,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static OrderDetailResponse from(Order order) {
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStore().getName(),
                order.getStatus(),
                order.getPickupDate(),
                order.getTotalPrice(),
                order.getOrderData(),
                order.getItems().stream()
                        .map(OrderItemResponse::from)
                        .toList(),
                order.getCreatedAt()
        );
    }
}
