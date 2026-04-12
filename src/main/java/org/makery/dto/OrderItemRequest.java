package org.makery.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "상품 ID는 필수입니다.")
        Long productId,

        @Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        int quantity,

        // 사용자가 AI를 통해 생성한 커스텀 케이크 이미지 URL
        String customizedImageUrl
) {}
