package org.makery.dto;

import org.makery.domain.OrderItem;

public record OrderItemResponse(
        Long id,
        String productName,      // 주문 당시의 상품명
        int quantity,            // 수량
        int unitPrice,           // 주문 당시의 단가
        String customizedImageUrl // AI 커스텀 이미지 URL
) {
    // 엔티티를 DTO로 변환하는 정적 팩토리 메서드
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getName(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getCustomizedImageUrl()
        );
    }
}
