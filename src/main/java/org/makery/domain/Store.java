package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "stores") // DB 예약어와 충돌 방지를 위해 테이블명 명시
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 매장명

    @Column(length = 1000)
    private String description; // 매장 소개

    private String hours; // 영업 시간

    private String notice; // 공지사항

    // --- 위치 정보 (위치 기반 검색용) ---
    private Double latitude;
    private Double longitude;

    // --- 평점 및 리뷰 통계 ---
    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    /**
     * 사장님별 커스텀 주문서 양식 (JSON)
     * 예: {"templates": [{"label": "맛 선택", "type": "select", "options": ["초코", "바닐라"]}]}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> orderSchema;

    /**
     * 사장님과의 관계
     * SellerProfile "1" --> "0..*" Store
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_profile_id")
    private SellerProfile sellerProfile;

    /**
     * 카테고리 관계
     * Store "1" --> "0..*" StoreCategory
     */
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StoreCategory> storeCategories = new ArrayList<>();

    /**
     * 포트폴리오 목록 (1:N)
     */
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Portfolio> portfolios = new ArrayList<>();

    // --- 비즈니스 로직 ---

    /**
     * 매장 정보 업데이트
     */
    public void update(String description, String hours, String notice) {
        this.description = description;
        this.hours = hours;
        this.notice = notice;
    }

    /**
     * 주문서 양식 업데이트
     */
    public void updateOrderSchema(Map<String, Object> newSchema) {
        this.orderSchema = newSchema;
    }
}
