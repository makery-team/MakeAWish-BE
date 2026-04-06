package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Store;

import java.util.Map;

@Builder
public record OrderSchemaResponse(
        Long storeId,
        String storeName,
        Map<String, Object> orderSchema
) {
    // 💡 Entity를 DTO로 바로 변환해주는 편의 메서드를 추가하면 서비스 코드가 깔끔해집니다!
    public static OrderSchemaResponse from(Store store) {
        return OrderSchemaResponse.builder()
                .storeId(store.getId())
                .storeName(store.getName())
                .orderSchema(store.getOrderSchema())
                .build();
    }
}