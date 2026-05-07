package org.makery.dto;

import lombok.Builder;
import org.makery.domain.AiInpaintedDesign;
import java.time.LocalDateTime;

@Builder
public record InpaintingResponse(
        Long id,
        Long portfolioId,        // 원본 디자인 ID
        String beforeImageUrl,   // 수정 전 이미지 (추가)
        String afterImageUrl,    // 수정 후 이미지 (기존 resultImageUrl 대체)
        String inpaintingPrompt, // 수정 요청 문구 (기존 prompt 대체)
        LocalDateTime createdAt
) {
    /**
     * 엔티티(AiInpaintedDesign)를 DTO로 변환하는 정적 팩토리 메서드
     */
    public static InpaintingResponse from(AiInpaintedDesign inpainting) {
        return InpaintingResponse.builder()
                .id(inpainting.getId())
                // 1. originPortfolio를 통해 원본 ID 추출
                .portfolioId(inpainting.getOriginPortfolio() != null ?
                        inpainting.getOriginPortfolio().getId() : null)
                // 2. 비포/애프터 이미지 매핑
                .beforeImageUrl(inpainting.getBeforeImageUrl())
                .afterImageUrl(inpainting.getAfterImageUrl())
                // 3. 인페인팅 전용 프롬프트 매핑
                .inpaintingPrompt(inpainting.getInpaintingPrompt())
                .createdAt(inpainting.getCreatedAt())
                .build();
    }
}