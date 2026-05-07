package org.makery.dto;

import org.makery.domain.AgentActionType;

public record AiAgentResponse(
        String message,
        AgentActionType actionType,
        Object data
) {}
