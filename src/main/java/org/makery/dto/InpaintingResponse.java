package org.makery.dto;

import lombok.Builder;
import org.makery.domain.AiInpaintedDesign;

import java.time.LocalDateTime;

@Builder
public record InpaintingResponse(
        Long id,
        Long portfolioId,
        String beforeImageUrl,
        String afterImageUrl,
        String inpaintingPrompt,
        LocalDateTime createdAt
) {
    public static InpaintingResponse from(AiInpaintedDesign inpainting) {
        return InpaintingResponse.builder()
                .id(inpainting.getId())
                .portfolioId(inpainting.getOriginPortfolio() != null ?
                        inpainting.getOriginPortfolio().getId() : null)
                .beforeImageUrl(inpainting.getBeforeImageUrl())
                .afterImageUrl(inpainting.getAfterImageUrl())
                .inpaintingPrompt(inpainting.getInpaintingPrompt())
                .createdAt(inpainting.getCreatedAt())
                .build();
    }
}
