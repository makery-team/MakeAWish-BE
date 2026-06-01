package org.makery.dto;

// AI 서버 -> 백엔드 (작업 완료 콜백)
public record AiWebhookRequest(
        Long task_id,         // 백엔드가 넘겨줬던 작업 DB ID
        String result_image,  // 완성된 이미지 데이터 (Base64)
        String status         // AI 서버 작업 결과 (예: "SUCCESS", "ERROR")
) {}
