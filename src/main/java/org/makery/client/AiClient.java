package org.makery.client;

import org.makery.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

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

    @PostMapping("/api/ai/reviews/summary")
    ReviewAiSummaryResponse getReviewSummary(@RequestBody AiReviewSummaryRequest request);

    @PostMapping("/api/ai/stores/profile-suggest")
    StoreAiProfileSuggestResponse suggestProfileImprovement(@RequestBody Map<String, Object> storeData);

    @PostMapping("/api/ai/stores/generate-bio")
    StoreAiBioGenerateResponse generateBio(@RequestBody Map<String, String> requestData);
}