package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (안정성 위해 protected)
@AllArgsConstructor
@Builder
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Column(nullable = false)
    private String imageUrl; // 케이크 디자인 시안 이미지 URL

    /**
     * AI 분석 및 사장님이 등록한 태그 리스트
     * JSON 타입으로 저장하여 태그의 유연한 추가/삭제 가능
     */
    @ManyToMany
    @JoinTable(
            name = "portfolio_tags",
            joinColumns = @JoinColumn(name = "portfolio_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    /**
     * AI 인페인팅 허용 여부 (ON/OFF)
     * 구매자가 이 시안을 바탕으로 에디터에서 수정할 수 있는지를 결정
     */
    @Builder.Default
    private boolean isInpaintingAllowed = true;

    /**
     * 매장과의 연관관계 (N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    /**
     * 통계 및 메타 데이터
     */
    @OneToMany(mappedBy = "portfolio")
    private Set<Like> likes = new HashSet<>();

    // --- 비즈니스 로직 --- //

//    /**
//     * 인페인팅 허용 상태 토글 및 태그 변경
//     */
//    public void updateInpaintingAndTags(boolean isInpaintingAllowed, List<String> tags) {
//        this.isInpaintingAllowed = isInpaintingAllowed;
//        this.tags = tags;
//    }
//
//    /**
//     * 좋아요 수 증가/감소
//     */
//    public void updateLikeCount(boolean isIncrement) {
//        if (isIncrement) {
//            this.likes++;
//        } else if (this.likeCount > 0) {
//            this.likeCount--;
//        }
//    }
}