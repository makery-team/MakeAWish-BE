package org.makery.domain;

/**
 * AI 상담 진행 상태
 */
public enum AiStatus {
    IN_PROGRESS, // 정보 수집 중
    COMPLETED,   // 정보 수집 완료
    FAILED       // 처리 실패
}
