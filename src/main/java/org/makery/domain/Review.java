package org.makery.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "reviews")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 리뷰 내용

    private String imageUrl;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer rating; // 별점 (1~5)

    @Column(columnDefinition = "TEXT")
    private String replyContent; // 답글 내용

    private LocalDateTime replyCreatedAt; // 답글 작성시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    public void updateReview(String content, Integer rating, String imageUrl) {
        this.content = content;
        this.rating = rating;
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    // 답글 작성 및 수정 메서드
    public void updateReply(String replyContent) {
        this.replyContent = replyContent;
        this.replyCreatedAt = LocalDateTime.now();
    }

    // 답글 삭제 메서드
    public void deleteReply() {
        this.replyContent = null;
        this.replyCreatedAt = null;
    }
}
