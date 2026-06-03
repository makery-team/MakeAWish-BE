package org.makery.service.handler;

import org.makery.domain.AgentActionType;
import org.makery.domain.User;
import org.makery.dto.AiAgentResponse;
import org.makery.dto.AiIntentResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * AI 서버에서 반환한 주문 관련 액션(SHOW_SCHEMA, CONFIRM_SLOTS, ORDER_SUMMARY)을
 * 프론트엔드로 안전하게 전달(Bypass)해 주는 핸들러입니다.
 */
@Component
public class OrderIntentHandler implements IntentHandler {

    @Override
    public boolean supports(AgentActionType actionType) {
        // 이 핸들러가 처리할 AI 액션 타입들
        return actionType == AgentActionType.SHOW_SCHEMA ||
                actionType == AgentActionType.CONFIRM_SLOTS ||
                actionType == AgentActionType.ORDER_SUMMARY;
    }

    @Override
    public AiAgentResponse handle(User user, AiIntentResponse aiResponse) {
        // 1. 디버깅용 로그 (서버 로그에서 데이터가 제대로 넘어오는지 확인 가능)
        // System.out.println("DEBUG: AI Message=" + aiResponse.message() + ", Action=" + aiResponse.actionType());

        // 2. 데이터가 null일 경우 NPE(NullPointerException) 방지를 위해 빈 Map으로 초기화
        Map<String, Object> data = (aiResponse.data() != null) ? aiResponse.data() : Collections.emptyMap();

        // 3. 응답 생성
        // actionType이 null일 경우 Enum.valueOf() 과정에서 에러가 날 수 있으니
        // aiResponse.actionType()이 올바른지 다시 한번 확인해보세요.
        return new AiAgentResponse(
                aiResponse.message(),
                aiResponse.actionType(),
                data
        );
    }
}