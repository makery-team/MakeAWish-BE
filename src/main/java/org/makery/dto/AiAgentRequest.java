package org.makery.dto;

public record AiAgentRequest(
        String message,
        Long productId
) {}
