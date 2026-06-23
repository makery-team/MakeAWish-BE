package org.makery.dto;

// 백엔드 -> AI 서버 (비동기 작업 지시)
public record InpaintingAiAsyncRequest(
        String prompt,
        String image_url,
        String image_b64,   // 연속 편집용 (Base64)
        String mask_b64,
        Long task_id,       // 작업 추적용 DB ID
        String webhook_url  // 작업 완료 후 결과를 받을 백엔드 주소
) {}
