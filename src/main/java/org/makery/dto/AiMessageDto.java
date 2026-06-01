package org.makery.dto;

public record AiMessageDto(
        String role,    // "user" 또는 "assistant"
        String content  // 대화 내용
) {}
