package org.makery.service.handler;

import org.makery.domain.AgentActionType;
import org.makery.domain.User;
import org.makery.dto.AiAgentResponse;
import org.makery.dto.AiIntentResponse;
import org.springframework.stereotype.Component;

@Component
public class ChatIntentHandler implements IntentHandler {

    @Override
    public boolean supports(AgentActionType actionType) {
        // 일반 대화(SIMPLE_CHAT)이거나 처리할 수 없는 값일 경우 방어 로직
        return actionType == AgentActionType.SIMPLE_CHAT || actionType == null;
    }

    @Override
    public AiAgentResponse handle(User user, AiIntentResponse aiResponse) {
        return new AiAgentResponse(
                aiResponse.message() != null ? aiResponse.message() : "제가 이해하지 못했어요.",
                AgentActionType.SIMPLE_CHAT,
                null
        );
    }
}