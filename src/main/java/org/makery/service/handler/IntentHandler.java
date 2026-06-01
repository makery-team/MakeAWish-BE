package org.makery.service.handler;

import org.makery.domain.AgentActionType;
import org.makery.domain.User;
import org.makery.dto.AiAgentResponse;
import org.makery.dto.AiIntentResponse;

public interface IntentHandler {
    boolean supports(AgentActionType intent);
    AiAgentResponse handle(User user, AiIntentResponse aiResponse);
}
