package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "portfolios") // 💡 이 줄을 추가했습니다! 이제 DB에 테이블이 확실히 생길 거예요.
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Column(nullable = false)
    private String imageUrl;

    /**
     * 💡 JSONB 에러 방지를 위해 String/TEXT로 변경
     */
    @ManyToMany
    @JoinTable(
            name = "portfolio_tags",
            joinColumns = @JoinColumn(name = "portfolio_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Builder.Default
    private boolean isInpaintingAllowed = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    /**
     * 통계 및 메타 데이터
     */
    @OneToMany(mappedBy = "portfolio")
    private Set<Like> likes = new HashSet<>();

    // --- 비즈니스 로직 --- //
    public void updateInpaintingAndTags(boolean isInpaintingAllowed, Set<Tag> tags) {
        this.isInpaintingAllowed = isInpaintingAllowed;
        this.tags = tags;
    }

    public int getLikeCount() {
        return this.likes.size();
    }

    public void addLike(Like like) {
        this.likes.add(like);
        like.setPortfolio(this);
    }

    public void removeLike(Like like) {
        this.likes.remove(like);
        like.setPortfolio(null);
    }
}