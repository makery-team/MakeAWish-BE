package org.makery.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 메시지 발신 주체 구분
 */
@Getter
@RequiredArgsConstructor
public enum SenderRole {
    USER("사용자"),
    ASSISTANT("AI 에이전트");

    private final String description;
}
