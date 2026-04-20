package org.makery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.makery.domain.Product;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name; // 예: "도시락 케이크"
    private int price;
    private String description;

    // 💡 핵심 수정: 주문 양식이 이제 여기에 위치합니다.
    private Map<String, Object> orderSchema;

    // 💡 핵심 수정: 해당 카테고리에 속한 디자인 샘플들만 담깁니다.
    private List<PortfolioResponse> portfolios;

    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .orderSchema(product.getOrderSchema())
                .portfolios(product.getPortfolios().stream()
                        .map(PortfolioResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}