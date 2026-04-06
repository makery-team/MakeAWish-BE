package org.makery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.makery.domain.Store;
import java.util.List; // 💡 List 사용을 위해 추가
import java.util.Map;
import java.util.stream.Collectors; // 💡 변환을 위해 추가

@Getter
@AllArgsConstructor
@Builder
public class StoreResponse {
    private Long id;
    private String name;
    private String description;
    private String hours;
    private Double rating;
    private Integer reviewCount;
    private Double latitude;
    private Double longitude;
    private Map<String, Object> orderSchema;

    // 💡 상세 조회 시 함께 보여줄 포트폴리오 리스트 추가!
    private List<PortfolioResponse> portfolios;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .hours(store.getHours())
                .rating(store.getRating())
                .reviewCount(store.getReviewCount())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .orderSchema(store.getOrderSchema())
                // 💡 매장이 가지고 있는 Portfolio 엔티티들을 DTO로 변환해서 담아줍니다.
                .portfolios(store.getPortfolios().stream()
                        .map(PortfolioResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}