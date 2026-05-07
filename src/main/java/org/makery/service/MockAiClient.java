package org.makery.service;

import org.makery.domain.AiStatus;
import org.makery.domain.AiIntent;
import org.makery.dto.AiIntentResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class MockAiClient implements AiClient {

    @Override
    public AiIntentResponse analyzeIntent(String userMessage) {
        return new AiIntentResponse(
                AiIntent.CHAT,           // 1. AiIntent
                Collections.emptyList(), // 2. List<String> tags (여기가 누락됐었습니다!)
                "안녕하세요! 무엇을 도와드릴까요?", // 3. String nextQuestion
                Collections.emptyMap(),  // 4. Map<String, Object> slots
                AiStatus.IN_PROGRESS     // 5. AiStatus
        );
    }
}