package org.makery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.makery.domain.Review;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private String nickname; // 작성자 닉네임
    private String content;  // 리뷰 내용
    private Integer rating;  // 별점
    private LocalDateTime createdAt; // 작성일

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .nickname(review.getUser().getNickname()) // 작성자 정보에서 닉네임 가져오기
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .build();
    }
}