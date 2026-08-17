package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private MethodType methodType;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    // 토스 결제 승인 키(paymentKey)를 저장할 필드
    private String pgTid;

    /**
     * 결제 완료 처리 메서드
     */
    public void completePayment(String paymentKey) {
        this.status = PaymentStatus.PAID;
        this.pgTid = paymentKey;
    }
}