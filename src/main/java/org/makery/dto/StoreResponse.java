package org.makery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.makery.domain.Store;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class StoreResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String hours;
    private String notice;
    private String cautionNotice; // 💡 추가된 매장 공통 주의사항
    private Double rating;
    private Integer reviewCount;
    private Double latitude;
    private Double longitude;

    // 💡 핵심 수정: 매장 직속 포트폴리오가 아니라, '카테고리(Product)' 리스트를 내려줍니다.
    private List<ProductResponse> categories;

    public static StoreResponse from(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .phone(store.getPhone())
                .hours(store.getHours())
                .notice(store.getNotice())
                .cautionNotice(store.getCautionNotice())
                .rating(store.getRating())
                .reviewCount(store.getReviewCount())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                // 💡 매장의 Product(카테고리)들을 DTO로 변환하여 담습니다.
                .categories(store.getProducts().stream()
                        .map(ProductResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}