package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Portfolio;
import org.makery.domain.Tag;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PortfolioResponse(
        Long portfolioId,
        Long storeId,
        String title,
        String description,
        String imageUrl,
        boolean isInpaintingAllowed,
        int likeCount,
        List<String> tags,
        LocalDateTime createdAt
) {
    public static PortfolioResponse from(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .portfolioId(portfolio.getId())
                .storeId(portfolio.getStore() != null ? portfolio.getStore().getId() : null)
                .title(portfolio.getTitle())
                .description(portfolio.getDescription())
                .imageUrl(portfolio.getImageUrl())
                .isInpaintingAllowed(portfolio.isInpaintingAllowed())
                .likeCount(portfolio.getLikeCount())
                .tags(portfolio.getTags() != null
                        ? portfolio.getTags().stream().map(Tag::getName).toList()
                        : List.of())
                .createdAt(portfolio.getCreatedAt())
                .build();
    }
}