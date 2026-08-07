package org.makery.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.makery.domain.Product;

@Getter
@AllArgsConstructor
@Builder
public class MyStoreProductResponse {
    private Long id;
    private String name;
    private int price;
    private String description;
    private Boolean isAvailable;

    // 💡 Object 타입으로 받아서 어떤 값이 들어오든 안전하게 처리합니다.
    private Object orderSchema;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static MyStoreProductResponse from(Product product) {
        Object schema = product.getOrderSchema();

        // orderSchema가 String 형태로 넘어왔을 때 JSON 파싱을 시도하고, 실패해도 에러를 내지 않고 텍스트 그대로 보냅니다.
        if (schema instanceof String schemaStr) {
            try {
                schema = objectMapper.readTree(schemaStr);
            } catch (Exception e) {
                schema = schemaStr;
            }
        }

        return MyStoreProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .isAvailable(product.isAvailable())
                .orderSchema(schema)
                .build();
    }
}