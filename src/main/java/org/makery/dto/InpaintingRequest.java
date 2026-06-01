package org.makery.dto;

public record InpaintingRequest(
        String prompt,      // 예: "케이크 위에 딸기를 올려줘"
        String maskImage    // 프론트엔드에서 그린 마스킹 영역 (Base64 형식)
) {}
