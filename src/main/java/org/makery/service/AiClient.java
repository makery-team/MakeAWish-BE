package org.makery.service;

import org.makery.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-analysis-service", url = "${ai.server.url}")
public interface AiClient {

    @PostMapping("/api/ai/chat")
    AiIntentResponse analyzeIntent(@RequestBody AiIntentRequest request);

    // 대기하지 않고 비동기 요청만 전달 (반환값 최소화)
    @PostMapping("/api/ai/inpaint")
    void requestInpaintedImageAsync(@RequestBody InpaintingAiAsyncRequest request);

    // 이미지 URL 기반 AI 태그 추천 API
    @PostMapping("/api/ai/generate-tags")
    AiTagResponse generateTags(@RequestBody AiTagRequest request);
}
