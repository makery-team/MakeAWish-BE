package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Portfolio;
import org.makery.domain.Tag;

import java.util.List;

@Builder
public record PortfolioFeedResponse(
        Long id,
        String imageUrl,
        String storeName,
        Long storeId,
        Long productId,
        List<String> tags,
        int likeCount,
        boolean isInpaintingAllowed
) {
    public static PortfolioFeedResponse from(Portfolio portfolio) {
        List<String> tagList = portfolio.getTags() != null
                ? new java.util.ArrayList<>(portfolio.getTags().stream().map(Tag::getName).toList())
                : new java.util.ArrayList<>();
        if (portfolio.getPrimaryTag() != null && !portfolio.getPrimaryTag().isBlank()) {
            tagList.remove(portfolio.getPrimaryTag());
            tagList.add(0, portfolio.getPrimaryTag());
        }

        return PortfolioFeedResponse.builder()
                .id(portfolio.getId())
                .imageUrl(portfolio.getImageUrl())
                .storeName(portfolio.getStore().getName())
                .storeId(portfolio.getStore().getId())
                .productId(portfolio.getProduct() != null ? portfolio.getProduct().getId() : null)
                .tags(tagList)
                .likeCount(portfolio.getLikeCount())
                .isInpaintingAllowed(portfolio.isInpaintingAllowed())
                .build();
    }
}