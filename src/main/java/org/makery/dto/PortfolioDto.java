package org.makery.dto;

public record PortfolioDto(
        Long id,
        String title,
        String imageUrl,
        String storeName
) {
    public static PortfolioDto fromEntity(org.makery.domain.Portfolio portfolio) {
        return new PortfolioDto(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getImageUrl(),
                portfolio.getStore().getName()
        );
    }
}
