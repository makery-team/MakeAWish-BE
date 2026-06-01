package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.AgentActionType;
import org.makery.domain.AiAgentMessage;
import org.makery.domain.SenderRole;
import org.makery.domain.User;
import org.makery.dto.AiAgentResponse;
import org.makery.dto.AiIntentRequest;
import org.makery.dto.AiIntentResponse;
import org.makery.dto.AiMessageDto;
import org.makery.repository.AiAgentMessageRepository;
import org.makery.service.handler.IntentHandler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiAgentService {

    private final AiClient aiClient;
    private final AiAgentMessageRepository messageRepository;
    private final List<IntentHandler> intentHandlers; // 스프링이 핸들러 구현체들을 자동 주입

    @Transactional
    public AiAgentResponse handleUserChat(User user, String userMessage) {

        // 1. 이전 대화 내역 조회
        List<AiAgentMessage> pastMessages = messageRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
        List<AiMessageDto> chatHistory = pastMessages.stream()
                .map(msg -> new AiMessageDto(
                        msg.getSenderRole().name().toLowerCase(),
                        msg.getMessage()
                ))
                .toList();

        // 2. 현재 사용자 질문 저장
        saveMessage(user, userMessage, SenderRole.USER, null, null);

        // 3. AI 서버 통신
        AiIntentRequest request = new AiIntentRequest(chatHistory, userMessage);
        AiIntentResponse aiResponse = aiClient.analyzeIntent(request);

        // (로그 출력 - 선택사항)
        System.out.println("AI 서버가 분석한 액션(ActionType): " + aiResponse.actionType());

        // ★★★ 여기를 수정해야 합니다! .intent() -> .actionType() ★★★
        AiAgentResponse finalResponse = intentHandlers.stream()
                .filter(handler -> handler.supports(aiResponse.actionType())) // 수정됨
                .findFirst()
                .map(handler -> handler.handle(user, aiResponse))
                .orElseThrow(() -> new IllegalStateException("확실하지 않음: 정의되지 않은 AI 액션입니다. 수신된 액션 = " + aiResponse.actionType())); // 수정됨

        // 4. AI 답변 기록 (비동기)
        saveAiResponseAsync(user, finalResponse);

        return finalResponse;
    }

    private void saveMessage(User user, String message, SenderRole role, AgentActionType actionType, Object data) {
        messageRepository.save(AiAgentMessage.builder()
                .user(user)
                .message(message)
                .senderRole(role)
                .actionType(actionType)
                .actionData(data instanceof Map ? (Map<String, Object>) data : null)
                .build());
    }

    @Async("aiAgentExecutor")
    public void saveAiResponseAsync(User user, AiAgentResponse response) {
        saveMessage(user, response.message(), SenderRole.ASSISTANT, response.actionType(), response.data());
    }
}
