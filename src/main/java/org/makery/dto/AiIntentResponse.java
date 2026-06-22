package org.makery.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.makery.domain.AgentActionType;

import java.util.Map;

public record AiIntentResponse(
        AgentActionType actionType,
        String message,
        Map<String, Object> data,
        Map<String, Object> extracted_slots,
        String status
) {
    @JsonCreator
    public static AiIntentResponse fromJson(
            @JsonProperty("actionType") String actionTypeStr,
            @JsonProperty("message") String message,
            @JsonProperty("data") Map<String, Object> data,
            @JsonProperty("extracted_slots") Map<String, Object> extracted_slots,
            @JsonProperty("status") String status
    ) {
        AgentActionType parsedType;
        try {
            parsedType = (actionTypeStr != null) ? AgentActionType.valueOf(actionTypeStr) : AgentActionType.SIMPLE_CHAT;
        } catch (IllegalArgumentException e) {
            // 환각이나 알 수 없는 액션타입이 들어오면 기본 대화 모드로 우회 (HTTP 500 에러 방어)
            parsedType = AgentActionType.SIMPLE_CHAT;
        }
        
        return new AiIntentResponse(parsedType, message, data, extracted_slots, status);
    }
}
