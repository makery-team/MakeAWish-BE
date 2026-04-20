package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int quantity;

    private int unitPrice;

    /**
     * 핵심 수정: 사용자가 주문 시 선택한 원본 포트폴리오 디자인
     * 사장님 페이지에서 이 이미지를 클릭하면 주문 시 참고한 시안을 볼 수 있습니다.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    /**
     * AI 시안 편집을 통해 탄생한 최종 커스텀 이미지 URL
     */
    private String customizedImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}