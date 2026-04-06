package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Inpainting;
import java.time.LocalDateTime;

@Builder
public record InpaintingResponse(
        Long id,
        Long portfolioId,
        String resultImageUrl,
        String prompt,
        LocalDateTime createdAt
) {
    public static InpaintingResponse from(Inpainting inpainting) {
        return InpaintingResponse.builder()
                .id(inpainting.getId())
                .portfolioId(inpainting.getPortfolio().getId())
                .resultImageUrl(inpainting.getResultImageUrl())
                .prompt(inpainting.getPrompt())
                .createdAt(inpainting.getCreatedAt())
                .build();
    }
}