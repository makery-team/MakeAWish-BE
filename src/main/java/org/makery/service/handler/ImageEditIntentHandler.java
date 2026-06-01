package org.makery.service.handler;

import org.makery.domain.AgentActionType;
import org.makery.domain.User;
import org.makery.dto.AiAgentResponse;
import org.makery.dto.AiIntentResponse;
import org.springframework.stereotype.Component;

@Component
public class ImageEditIntentHandler implements IntentHandler {

    @Override
    public boolean supports(AgentActionType actionType) {
        return actionType == AgentActionType.EDIT_IMAGE;
    }

    @Override
    public AiAgentResponse handle(User user, AiIntentResponse aiResponse) {
        return new AiAgentResponse(
                aiResponse.message() != null ? aiResponse.message() : "에디터 화면에서 이미지를 수정해보세요!",
                AgentActionType.EDIT_IMAGE,
                aiResponse.data()
        );
    }
}
