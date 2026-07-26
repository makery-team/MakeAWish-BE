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
    private Long storeId;
    private Long orderId;
    private Long userId;
    private String nickname; // 작성자 닉네임
    private String content;  // 리뷰 내용
    private Integer rating;  // 별점
    private String imageUrl; // 케이크 사진
    private String storeName; // 매장 이름
    private LocalDateTime createdAt; // 작성일
    private String replyContent;      // 답글 내용
    private LocalDateTime replyCreatedAt; // 답글 작성일

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .storeId(review.getStore() != null ? review.getStore().getId() : null)
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .userId(review.getOrder() != null && review.getOrder().getUser() != null ? review.getOrder().getUser().getId() : null)
                .nickname(review.getOrder() != null && review.getOrder().getUser() != null ? review.getOrder().getUser().getNickname() : null)
                .content(review.getContent())
                .rating(review.getRating())
                .imageUrl(review.getImageUrl())
                .storeName(review.getStore() != null ? review.getStore().getName() : null)
                .createdAt(review.getCreatedAt())
                .replyContent(review.getReplyContent())
                .replyCreatedAt(review.getReplyCreatedAt())
                .build();
    }
}