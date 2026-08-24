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
        boolean hasReview,
        Long customerId,
        String customerName,
        String customerPhone,
        String rejectReason
) {
    public static OrderSummaryResponse from(Order order) {
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

        String storeName = (order.getStore() != null && order.getStore().getName() != null)
                ? order.getStore().getName()
                : "매장";

        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                storeName,
                order.getStatus(),
                order.getTotalPrice(),
                order.getExtraFee() != null ? order.getExtraFee() : 0,
                order.getExtraFeeReason(),
                order.getPickupDate(),
                order.getCreatedAt(),
                order.getReview() != null,
                userId,
                name,
                phone,
                order.getRejectReason()
        );
    }
}
