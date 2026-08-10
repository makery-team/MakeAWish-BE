package org.makery.dto;

public record InpaintingRequest(
        String prompt,      // 예: "케이크 위에 딸기를 올려줘"
        String maskImage,   // 프론트엔드에서 그린 마스킹 영역 (Base64 형식)
        String currentImage, // 연속 편집을 위한 현재 편집된 이미지 (Base64 또는 URL 형식, nullable)
        String referenceImage // 프론트엔드에서 업로드한 레퍼런스 이미지 (Base64 형식, nullable)
) {}
