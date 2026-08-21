package org.makery.service.handler;

import lombok.RequiredArgsConstructor;
import org.makery.domain.AgentActionType;
import org.makery.domain.User;
import org.makery.dto.AiAgentResponse;
import org.makery.dto.AiIntentResponse;
import org.makery.dto.PortfolioDto;
import org.makery.repository.PortfolioRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SearchIntentHandler implements IntentHandler {

    private final PortfolioRepository portfolioRepository;

    @Override
    public boolean supports(AgentActionType actionType) {
        // AI가 "포트폴리오 리스트를 보여줘라"라고 판별했을 때 작동
        return actionType == AgentActionType.PORTFOLIO_LIST;
    }

    @Override
    public AiAgentResponse handle(User user, AiIntentResponse aiResponse) {
        List<PortfolioDto> results = Collections.emptyList();

        // AI가 준 data 필드에서 tags 추출 (Map 형태 처리)
        if (aiResponse.data() != null && aiResponse.data().containsKey("tags")) {
            List<String> rawTags = (List<String>) aiResponse.data().get("tags");
            if (rawTags != null && !rawTags.isEmpty()) {
                // 단어별 하드코딩 없이, 모든 키워드에 대해 동적으로 '케이크' 접미사 유무 상호 변환 지원 (예: '생일' <-> '생일케이크', '루피' <-> '루피케이크')
                Set<String> searchKeywords = new LinkedHashSet<>();
                for (String raw : rawTags) {
                    if (raw == null || raw.isBlank()) continue;
                    String clean = raw.replace("#", "").trim();
                    searchKeywords.add(clean);

                    String root = clean.replaceAll("케이크|cake", "").trim();
                    if (!root.isEmpty()) {
                        searchKeywords.add(root);
                        searchKeywords.add(root + "케이크");
                        searchKeywords.add(root + " 케이크");
                    }
                }

                results = portfolioRepository.findByTagNamesRanked(new ArrayList<>(searchKeywords))
                        .stream().map(PortfolioDto::fromEntity).toList();
            }
        }

        // 태그 매칭 결과가 없거나 태그가 없는 경우 전체 인기 포트폴리오 정렬 추천
        if (results.isEmpty()) {
            results = portfolioRepository.findAllRanked()
                    .stream().map(PortfolioDto::fromEntity).toList();
        }

        return new AiAgentResponse(
                aiResponse.message() != null ? aiResponse.message() : "추천 디자인입니다!",
                AgentActionType.PORTFOLIO_LIST,
                results
        );
    }
}
