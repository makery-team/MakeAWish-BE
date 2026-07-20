package org.makery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class PortfolioRegisterRequest {

    private String title;
    private String description;

    @NotBlank(message = "포트폴리오 케이크 이미지는 필수입니다.")
    private String imageUrl;

    @NotNull(message = "상위 제품(카테고리) ID는 필수입니다.")
    private Long productId;

    private List<String> tags; // 최초 등록 시 함께 포함할 태그 목록 (AI 추천 태그 등)
}
