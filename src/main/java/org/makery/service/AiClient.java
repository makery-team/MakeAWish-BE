package org.makery.service;

import org.makery.dto.AiIntentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 외부 AI 분석 서버와의 통신을 담당하는 클라이언트
 */
@FeignClient(name = "ai-analysis-service", url = "${ai.server.url}")
public interface AiClient {

    /**
     * 사용자의 메시지를 AI 서버로 전송하여 의도(Intent)와 데이터(Slots)를 분석함
     */
    @PostMapping("/api/v1/analyze")
    AiIntentResponse analyzeIntent(@RequestBody String message);
}