package org.makery.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PortfolioTagRecommendRequest {

    @NotBlank(message = "분석할 케이크 이미지 URL은 필수입니다.")
    private String imageUrl;
}
