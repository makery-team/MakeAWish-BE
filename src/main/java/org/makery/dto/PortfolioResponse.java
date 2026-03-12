package org.makery.dto;

import org.makery.domain.Portfolio;
import java.util.List;

public record PortfolioResponse(
        Long id,
        String imageUrl,
        List<String> tags,
        boolean isInpaintingAllowed,
        Integer likeCount
) {
    public static PortfolioResponse from(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getImageUrl(),
                portfolio.getTags(),
                portfolio.isInpaintingAllowed(),
                portfolio.getLikeCount()
        );
    }
}