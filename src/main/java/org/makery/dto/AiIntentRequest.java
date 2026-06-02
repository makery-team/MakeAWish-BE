package org.makery.dto;

import java.util.List;
import java.util.Map;

public record AiIntentRequest(
        List<AiMessageDto> messages, // 이전 대화 내역 (선택적)
        String current_message,      // 현재 사용자가 입력한 메시지 (필수)
        Map<String, Object> schema_json // 파이썬 AI 서버로 던져줄 스키마 정보
) {}
