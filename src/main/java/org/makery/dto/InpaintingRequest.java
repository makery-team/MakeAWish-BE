package org.makery.dto;

public record InpaintingRequest(
        String prompt,      // 예: "케이크 위에 '21'이라는 숫자를 써줘"
        String maskImage    // 수정할 영역이 표시된 이미지 데이터 (보통 Base64 문자열)
) {}