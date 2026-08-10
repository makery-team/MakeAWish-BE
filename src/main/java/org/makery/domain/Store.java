package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.makery.dto.StoreProfileUpdateRequest;

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

    @Column(length = 500)
    private String address;

    @Column(length = 50)
    private String phone;

    @Column(name = "hours_json", columnDefinition = "TEXT")
    private String hours;
    
    private String notice; // 사장님 한마디/공지

    /**
     * 핵심 수정: 알러지 및 매장 공통 주의사항 안내
     * AI 상담 및 주문 전 체크박스용 가이드로 활용됩니다.
     */
    @Column(columnDefinition = "TEXT")
    private String cautionNotice;

    private Double latitude;
    private Double longitude;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer reviewCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_profile_id")
    private SellerProfile sellerProfile;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Portfolio> portfolios = new ArrayList<>();

    @Column(length = 1000)
    private String imageUrl;

    // 이미지 업데이트를 위한 비즈니스 메서드
    public void updateProfile(StoreProfileUpdateRequest request) {
        this.name = request.getName();
        this.description = request.getDescription();
        this.address = request.getAddress();
        this.phone = request.getPhone();
        this.hours = request.getHours();
        this.notice = request.getNotice();
        this.cautionNotice = request.getCautionNotice();
        this.imageUrl = request.getImageUrl(); // 이미지 추가
    }
}