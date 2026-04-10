package org.makery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record OrderRequest(
        @NotNull(message = "매장 ID는 필수입니다.")
        Long storeId,

        @NotNull(message = "픽업 날짜를 선택해 주세요.")
        LocalDate pickupDate,

        // 케이크 문구, 요청 사항 등 TEXT 데이터
        String orderData,

        @NotEmpty(message = "주문할 상품을 최소 하나 이상 선택해 주세요.")
        @Valid // 내부 리스트 객체들의 유효성도 검사함
        List<OrderItemRequest> items
) {}
