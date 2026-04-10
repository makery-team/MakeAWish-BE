package org.makery.domain;

public enum OrderStatus {

    PENDING_QUOTE,  // 견적 요청 상태 (아직 견적 미작성)
    QUOTED,         // 견적 완료 상태
    PAID,           // 결제 완료 상태
    IN_PROGRESS,    // 작업 진행 중
    PICKUP_READY,   // 픽업 준비 완료
    COMPLETED,      // 작업 및 주문 최종 완료
    CANCELED        // 주문 취소 상태
}
