package org.makery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.makery.domain.Store;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@Builder
public class MyStoreResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String hours;
    private String notice;
    private String cautionNotice;
    private String keywords;
    private Double rating;
    private Integer reviewCount;
    private Double latitude;
    private Double longitude;
    private String imageUrl;
    private List<MyStoreProductResponse> categories;

    public static MyStoreResponse from(Store store) {
        return MyStoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .phone(store.getPhone())
                .hours(store.getHours())
                .notice(store.getNotice())
                .cautionNotice(store.getCautionNotice())
                .keywords(store.getKeywords())
                .rating(store.getRating())
                .reviewCount(store.getReviewCount())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .imageUrl(store.getImageUrl())
                .categories(store.getProducts() != null ?
                        store.getProducts().stream()
                                .map(MyStoreProductResponse::from)
                                .collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }
}