package org.makery.dto;

import java.util.List;

public record AiIntentRequest(
        List<AiMessageDto> messages, // 이전 대화 내역 (선택적)
        String current_message       // 현재 사용자가 입력한 메시지 (필수)
) {}
