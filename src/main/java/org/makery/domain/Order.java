package org.makery.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime pickupDate;

    private int totalPrice;

    @Builder.Default
    @Column(name = "extra_fee")
    private Integer extraFee = 0;

    @Column(name = "extra_fee_reason", columnDefinition = "TEXT")
    private String extraFeeReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> orderData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order")
    private Payment payment;

    public void calculateTotalPrice() {
        int basePrice = items.stream()
                .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                .sum();

        int extra = (this.extraFee != null) ? this.extraFee : 0;
        this.totalPrice = basePrice + extra;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (this.status == OrderStatus.COMPLETED || this.status == OrderStatus.CANCELED) {
            throw new IllegalStateException("이미 종료된 주문은 상태를 변경할 수 없습니다.");
        }
        this.status = newStatus;
    }

    /**
     * 추가금 반영 및 총액 재계산
     */
    public void updateExtraFee(int extraFee, String reason) {
        int previousExtraFee = (this.extraFee != null) ? this.extraFee : 0;
        int basePrice = this.totalPrice - previousExtraFee;

        this.extraFee = extraFee;
        this.extraFeeReason = reason;
        this.totalPrice = basePrice + extraFee;
    }
}
