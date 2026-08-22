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
        boolean hasReview,
        Long customerId,
        String customerName,
        String customerPhone,
        String rejectReason
) {
    public static OrderDetailResponse from(Order order) {
        String name = "주문 고객";
        String phone = null;
        Long userId = null;

        if (order.getUser() != null) {
            userId = order.getUser().getId();
            if (order.getUser().getName() != null && !order.getUser().getName().isBlank()) {
                name = order.getUser().getName();
            } else if (order.getUser().getNickname() != null && !order.getUser().getNickname().isBlank()) {
                name = order.getUser().getNickname();
            }
            phone = order.getUser().getPhoneNumber();
        }

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
                order.getReview() != null,
                userId,
                name,
                phone,
                order.getRejectReason()
        );
    }
}