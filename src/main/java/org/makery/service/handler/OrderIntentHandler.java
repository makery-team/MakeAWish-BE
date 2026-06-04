package org.makery.service.handler;

import org.makery.domain.AgentActionType;
import org.makery.domain.User;
import org.makery.dto.AiAgentResponse;
import org.makery.dto.AiIntentResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI가 반환한 대화형 주문 관련 액션(SHOW_SCHEMA, CONFIRM_SLOTS, ORDER_SUMMARY)을
 * 프론트엔드로 그대로 바이패스(전달)해 주는 핸들러입니다.
 */
@Component
public class OrderIntentHandler implements IntentHandler {

    @Override
    public boolean supports(AgentActionType actionType) {
        return actionType == AgentActionType.SHOW_SCHEMA ||
               actionType == AgentActionType.CONFIRM_SLOTS ||
               actionType == AgentActionType.ORDER_SUMMARY;
    }

    @Override
    public AiAgentResponse handle(User user, AiIntentResponse aiResponse) {
        // AI가 파싱해서 넘겨준 data (또는 extracted_slots) 객체를 그대로 프론트엔드로 전달합니다.
        
        // 2. 응답 데이터(data)를 수정 가능한 HashMap으로 변환
        Map<String, Object> responseData = (aiResponse.data() != null) 
                ? new java.util.HashMap<>(aiResponse.data()) 
                : new java.util.HashMap<>();
        
        // ★ 필수: 챗봇 화면에 표시하기 위해 추출된 슬롯 데이터를 응답에 포함
        if (aiResponse.extracted_slots() != null) {
            responseData.put("extracted_slots", aiResponse.extracted_slots());
        }

        String message = (aiResponse.message() != null && !aiResponse.message().isBlank()) 
                ? aiResponse.message() 
                : "주문 내용을 확인해주세요.";

        return new AiAgentResponse(
                message,
                aiResponse.actionType(),
                responseData
        );
    }
}
