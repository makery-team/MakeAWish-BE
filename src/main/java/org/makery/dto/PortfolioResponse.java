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
        Long productId,
        String storeName,
        String title,
        String description,
        String imageUrl,
        boolean isInpaintingAllowed,
        int likeCount,
        List<String> tags,
        LocalDateTime createdAt
) {
    public static PortfolioResponse from(Portfolio portfolio) {
        List<String> tagList = portfolio.getTags() != null
                ? new java.util.ArrayList<>(portfolio.getTags().stream().map(Tag::getName).toList())
                : new java.util.ArrayList<>();
        if (portfolio.getPrimaryTag() != null && !portfolio.getPrimaryTag().isBlank()) {
            tagList.remove(portfolio.getPrimaryTag());
            tagList.add(0, portfolio.getPrimaryTag());
        }

        return PortfolioResponse.builder()
                .portfolioId(portfolio.getId())
                .storeId(portfolio.getStore() != null ? portfolio.getStore().getId() : null)
                .productId(portfolio.getProduct() != null ? portfolio.getProduct().getId() : null)
                .storeName(portfolio.getStore() != null ? portfolio.getStore().getName() : null)
                .title(portfolio.getTitle())
                .description(portfolio.getDescription())
                .imageUrl(portfolio.getImageUrl())
                .isInpaintingAllowed(portfolio.isInpaintingAllowed())
                .likeCount(portfolio.getLikeCount())
                .tags(tagList)
                .createdAt(portfolio.getCreatedAt())
                .build();
    }
}