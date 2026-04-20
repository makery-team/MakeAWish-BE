package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Product;

import java.util.Map;

@Builder
public record OrderSchemaResponse(
        Long productId,      // 💡 StoreId 대신 ProductId(카테고리 ID)를 사용
        String productName,  // 💡 예: "도시락 케이크", "레터링 케이크"
        Map<String, Object> orderSchema
) {
    /**
     * 💡 핵심 수정: 이제 Store가 아닌 Product 엔티티를 전달받아
     * 해당 카테고리의 양식을 DTO로 변환합니다.
     */
    public static OrderSchemaResponse from(Product product) {
        return OrderSchemaResponse.builder()
                .productId(product.getId())
                .productName(product.getName())
                .orderSchema(product.getOrderSchema()) // Product에 저장된 양식 호출
                .build();
    }
}