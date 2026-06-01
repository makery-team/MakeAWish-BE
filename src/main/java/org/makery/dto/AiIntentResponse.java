package org.makery.dto;

import org.makery.domain.AgentActionType;

import java.util.Map;

public record AiIntentResponse(
        AgentActionType actionType,
        String message,
        Map<String, Object> data,
        Map<String, Object> extracted_slots,
        String status
) {}
