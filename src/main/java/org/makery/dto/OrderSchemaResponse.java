package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Store;

@Builder
public record OrderSchemaResponse(
        Long storeId,
        String storeName,
        String orderSchema // 💡 DB에 저장된 JSON 문자열 (예: "{"size": "mini", ...}")
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