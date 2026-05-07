package org.makery.dto;

import org.makery.domain.AiIntent;
import org.makery.domain.AiStatus;

import java.util.List;
import java.util.Map;

public record AiIntentResponse(
        AiIntent intent,       // 사용자 의도 (예: SEARCH, ORDER)
        List<String> tags,   // 검색용 키워드 목록 (예: "분홍색", "도시락")
        String nextQuestion, // 사용자에게 전달할 다음 대화 문구
        Map<String, Object> slots, // 추출된 상세 정보 (맛, 사이즈, 문구 등)
        AiStatus status        // 의도 처리 상태 (진행 중, 완료 등)
) {}
