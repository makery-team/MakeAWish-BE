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
        Map<String, Object> responseData = aiResponse.data();
        
        // 파이썬 AI 서버가 extracted_slots를 data 밖으로 빼서 줬을 경우를 대비한 방어 로직
        if (responseData == null && aiResponse.extracted_slots() != null) {
            responseData = Map.of("extracted_slots", aiResponse.extracted_slots());
        }

        return new AiAgentResponse(
                aiResponse.message() != null ? aiResponse.message() : "주문 내용을 확인해주세요.",
                aiResponse.actionType(),
                responseData
        );
    }
}
