package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 예: "도시락 케이크", "레터링 케이크"

    private int price; // 카테고리별 기본 시작 가격

    private String description;

    private boolean isAvailable;

    /**
     * 핵심 수정: 카테고리별 커스텀 주문서 양식 (JSON)
     * 이 필드를 통해 제품군마다 다른 질문(맛, 옵션 등)을 구성합니다.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> orderSchema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    /**
     * 핵심 수정: 해당 카테고리에 속한 디자인 샘플(포트폴리오) 목록
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Portfolio> portfolios = new ArrayList<>();
}