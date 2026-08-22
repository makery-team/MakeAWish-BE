package org.makery.domain;

public enum NotificationType {
    ORDER,      // 주문 관련 (접수, 견적, 거절, 상태변경 등)
    PAYMENT,    // 결제 관련 (입금 완료)
    CHAT,       // 채팅 메시지 도착
    AI_SKETCH,  // AI 스케치/인페인팅 완성
    SYSTEM      // 시스템 공지 및 안내
}
