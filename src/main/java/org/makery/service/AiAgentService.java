package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.makery.domain.*;
import org.makery.dto.AiAgentResponse;
import org.makery.dto.AiIntentResponse;
import org.makery.dto.PortfolioDto;
import org.makery.repository.AiAgentMessageRepository;
import org.makery.repository.AiInpaintedDesignRepository; // 리포지토리 이름 변경 확인
import org.makery.repository.PortfolioRepository;
import org.makery.websocket.AwsS3Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiAgentService {

    private final AiClient aiClient;
    private final PortfolioRepository portfolioRepository;
    private final AiAgentMessageRepository aiAgentMessageRepository;
    private final AiInpaintedDesignRepository aiInpaintedDesignRepository;
    private final UserService userService;
    private final AwsS3Service awsS3Service;

    @Transactional
    public AiAgentResponse handleUserChat(Long userId, String userMessage) {
        User user = userService.findById(userId);

        // 1. 사용자 질문 기록
        aiAgentMessageRepository.save(AiAgentMessage.builder()
                .user(user)
                .message(userMessage)
                .senderRole(SenderRole.USER)
                .build());

        // 2. AI 서버로 질문 분석 요청
        AiIntentResponse aiResponse = aiClient.analyzeIntent(userMessage);

        // 3. 비즈니스 로직 처리 및 응답 구성
        AiAgentResponse finalResponse = switch (aiResponse.intent()) {
            case SEARCH -> {
                var list = portfolioRepository.findByTagNames(aiResponse.tags())
                        .stream().map(PortfolioDto::fromEntity).toList();
                yield new AiAgentResponse("추천 디자인입니다.", AgentActionType.PORTFOLIO_LIST, list);
            }
            case ORDER -> {
                if (aiResponse.status() == AiStatus.COMPLETED) {
                    int totalPrice = calculateTotalPrice(aiResponse.slots());
                    yield new AiAgentResponse(
                            String.format("최종 %d원입니다. 주문할까요?", totalPrice),
                            AgentActionType.ORDER_SUMMARY,
                            Map.of("slots", aiResponse.slots(), "totalPrice", totalPrice)
                    );
                }
                yield new AiAgentResponse(aiResponse.nextQuestion(), AgentActionType.SHOW_SCHEMA, aiResponse.slots());
            }
            case IMAGE_EDIT -> {
                // 3-1. 임시 URL 추출
                String tempUrl = (String) aiResponse.slots().get("edited_image_url");

                // 3-2. S3 영구 업로드 (AwsS3Service 인스턴스 메서드 호출)
                String permanentUrl = awsS3Service.uploadFromUrl(tempUrl);

                // 3-3. 원본 포트폴리오 식별 (ID 기반)
                Long portfolioId = Long.parseLong(aiResponse.slots().get("portfolioId").toString());
                Portfolio origin = portfolioRepository.findById(portfolioId)
                        .orElseThrow(() -> new EntityNotFoundException("원본 디자인을 찾을 수 없습니다."));

                // 3-4. 인페인팅 전용 자산 저장 (바뀐 필드명 적용)
                AiInpaintedDesign result = aiInpaintedDesignRepository.save(AiInpaintedDesign.builder()
                        .user(user)
                        .originPortfolio(origin)
                        .inpaintingPrompt(aiResponse.slots().getOrDefault("prompt", "수정 요청").toString())
                        .beforeImageUrl(origin.getImageUrl())
                        .afterImageUrl(permanentUrl)
                        .build());

                yield new AiAgentResponse(
                        "요청하신 대로 이미지를 수정해 보았어요!",
                        AgentActionType.INPAINTING_RESULT,
                        Map.of("inpaintingId", result.getId(), "url", permanentUrl)
                );
            }
            default -> new AiAgentResponse(aiResponse.nextQuestion(), AgentActionType.SIMPLE_CHAT, null);
        };

        // 4. AI 답변 기록 (비동기 처리)
        saveAiResponseAsync(user, finalResponse);

        return finalResponse;
    }

    @Async("aiAgentExecutor")
    public void saveAiResponseAsync(User user, AiAgentResponse response) {
        aiAgentMessageRepository.save(AiAgentMessage.builder()
                .user(user)
                .message(response.message())
                .senderRole(SenderRole.ASSISTANT)
                .actionType(response.actionType())
                .actionData(response.data() instanceof Map ? (Map<String, Object>) response.data() : null)
                .build());
    }

    private int calculateTotalPrice(Map<String, Object> slots) {
        int basePrice = 30000;
        int optionPrice = 0;

        if ("초코".equals(slots.get("flavor"))) optionPrice += 5000;
        if ("1호".equals(slots.get("size"))) optionPrice += 10000;

        return basePrice + optionPrice;
    }
}