package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "portfolios")
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

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String imageUrl;

    /**
     * 핵심 수정: 이 디자인 샘플이 속한 상위 제품군(카테고리)
     * 예: 이 티니핑 사진은 '도시락 케이크' 카테고리에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToMany
    @JoinTable(
            name = "portfolio_tags",
            joinColumns = @JoinColumn(name = "portfolio_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new java.util.LinkedHashSet<>();

    @Builder.Default
    private boolean isInpaintingAllowed = true;

    @Builder.Default
    private int likeCount = 0;

    public void updateInpaintingAndTags(boolean isInpaintingAllowed, Set<Tag> tags) {
        this.isInpaintingAllowed = isInpaintingAllowed;
        this.tags = tags;
    }
}