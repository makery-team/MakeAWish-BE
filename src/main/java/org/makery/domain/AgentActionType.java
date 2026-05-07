package org.makery.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AI가 프론트엔드에 지시하는 액션 유형 (UI 제어 신호)
 */
@Getter
@RequiredArgsConstructor
public enum AgentActionType {
    // 1. 일반 및 탐색
    SIMPLE_CHAT("단순 대화"),
    PORTFOLIO_LIST("디자인 추천 목록"),

    // 2. 이미지 편집 (AI 에디터)
    EDIT_IMAGE("이미지 편집 모드 진입"),    // 에디터 UI를 띄울 때 사용
    INPAINTING_RESULT("인페인팅 결과 표시"), // 편집 완료 후 결과 이미지를 보여줄 때 사용

    // 3. 주문 프로세스 (대화형 주문)
    SHOW_SCHEMA("주문 양식 표시"),         // 슬롯 필링 중 입력 폼 노출
    CONFIRM_SLOTS("주문 정보 확인"),        // 모든 정보 수집 완료 후 최종 확인
    ORDER_SUMMARY("주문 요약 및 결제 안내"); // 금액 계산 포함 최종 요약

    private final String description;
}