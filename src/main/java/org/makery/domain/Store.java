package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "stores")
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
    private String name;

    @Column(length = 1000)
    private String description;

    private String hours;
    private String notice;
    private Double latitude;
    private Double longitude;

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

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Portfolio> portfolios = new ArrayList<>();

    // 💡 리뷰와의 일대다(1:N) 관계 추가!
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /**
     * 매장 정보 업데이트
     */
    public void updateProfile(String description, String hours, String notice) {
        this.description = description;
        this.hours = hours;
        this.notice = notice;
    }

    public void updateOrderSchema(Map<String, Object> newSchema) {
        this.orderSchema = newSchema;
    }
}
