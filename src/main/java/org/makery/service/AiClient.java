package org.makery.service;

import org.makery.dto.AiIntentRequest;
import org.makery.dto.AiIntentResponse;
import org.makery.dto.InpaintingAiAsyncRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-analysis-service", url = "${ai.server.url}")
public interface AiClient {

    @PostMapping("/api/ai/chat")
    AiIntentResponse analyzeIntent(@RequestBody AiIntentRequest request);

    // 대기하지 않고 비동기 요청만 전달 (반환값 최소화)
    @PostMapping("/api/ai/inpaint/async")
    void requestInpaintedImageAsync(@RequestBody InpaintingAiAsyncRequest request);
}
