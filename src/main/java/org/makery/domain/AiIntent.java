package org.makery.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AI가 판단한 사용자의 의도 유형
 */
@Getter
@RequiredArgsConstructor
public enum AiIntent {
    SEARCH("디자인 검색"),
    ORDER("주문 상담"),
    IMAGE_EDIT("이미지 편집/인페인팅"), // 추가된 항목
    CHAT("일반 대화"),
    UNKNOWN("알 수 없음");

    private final String description;
}
