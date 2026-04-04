package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Portfolio;

@Builder
public record PortfolioResponse(
        Long id,
        String imageUrl,
        String tags, // 💡 List<String>에서 String으로 변경!
        boolean isInpaintingAllowed,
        Integer likeCount
) {
    public static PortfolioResponse from(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .imageUrl(portfolio.getImageUrl())
                .tags(portfolio.getTags()) // 💡 이제 타입이 String이라서 잘 맞을 거예요!
                .isInpaintingAllowed(portfolio.isInpaintingAllowed())
                .likeCount(portfolio.getLikeCount())
                .build();
    }
}