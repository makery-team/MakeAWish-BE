package org.makery.dto;

import lombok.Builder;
import lombok.Getter;
import org.makery.domain.Order;

@Getter
@Builder
public class ExtraFeeResponse {

    private Long orderId;
    private String orderNumber;
    private Integer basePrice;    // 기본 상품 가격 (추가금 제외)
    private Integer extraFee;     // 사장님이 책정한 추가금
    private Integer totalPrice;   // 최종 결제 금액 (기본가 + 추가금)
    private String reason;        // 추가금 산정 사유
    private String orderStatus;   // 현재 주문 상태

    public static ExtraFeeResponse from(Order order) {
        int extra = order.getExtraFee() != null ? order.getExtraFee() : 0;
        int base = order.getTotalPrice() - extra;

        return ExtraFeeResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .basePrice(base)
                .extraFee(extra)
                .totalPrice(order.getTotalPrice())
                .reason(order.getExtraFeeReason())
                .orderStatus(order.getStatus() != null ? order.getStatus().name() : null)
                .build();
    }
}
