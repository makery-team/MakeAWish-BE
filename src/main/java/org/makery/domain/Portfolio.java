package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios") // 💡 이 줄을 추가했습니다! 이제 DB에 테이블이 확실히 생길 거예요.
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    /**
     * 💡 JSONB 에러 방지를 위해 String/TEXT로 변경
     */
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;

    @Builder.Default
    private boolean isInpaintingAllowed = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Builder.Default
    private Integer likeCount = 0;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- 비즈니스 로직 ---
    public void updateInpaintingAndTags(boolean isInpaintingAllowed, String tags) {
        this.isInpaintingAllowed = isInpaintingAllowed;
        this.tags = tags;
    }

    public void updateLikeCount(boolean isIncrement) {
        if (isIncrement) {
            this.likeCount++;
        } else if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}