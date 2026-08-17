package org.makery.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PortfolioUpdateRequest {

    private String title;
    private String description;
    private String imageUrl;
    private Long productId;
    private Boolean isInpaintingAllowed; // 인페인팅 가능 여부 수정
    private List<String> tags;            // 수정 및 추가할 태그 목록
}
