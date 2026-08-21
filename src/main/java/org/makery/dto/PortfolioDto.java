package org.makery.dto;

import org.makery.domain.Tag;
import java.util.List;

public record PortfolioDto(
        Long id,
        String title,
        String imageUrl,
        String storeName,
        Long storeId,
        Long productId,
        List<String> tags
) {
    public static PortfolioDto fromEntity(org.makery.domain.Portfolio portfolio) {
        return new PortfolioDto(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getImageUrl(),
                portfolio.getStore().getName(),
                portfolio.getStore().getId(),
                portfolio.getProduct() != null ? portfolio.getProduct().getId() : null,
                portfolio.getTags() != null ? portfolio.getTags().stream().map(Tag::getName).toList() : List.of()
        );
    }
}
