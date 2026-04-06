package org.makery.dto;

import lombok.Builder;
import org.makery.domain.Like;
import org.makery.domain.Portfolio;
import org.makery.domain.Tag; // 반드시 우리 프로젝트의 Tag 엔티티를 임포트해야 합니다.

import java.util.Set;

@Builder
public record PortfolioResponse(
        Long id,
        String imageUrl,
        Set<Tag> tags,                // 엔티티 타입 그대로 유지
        boolean isInpaintingAllowed,
        Set<Like> likes               // 엔티티 타입 그대로 유지
) {
    public static PortfolioResponse from(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getImageUrl(),
                portfolio.getTags(),  // 변환 없이 바로 Set 전달
                portfolio.isInpaintingAllowed(),
                portfolio.getLikes()  // 변환 없이 바로 Set 전달
        );
    }
}