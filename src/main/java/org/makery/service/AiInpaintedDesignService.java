package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.*;
import org.makery.dto.InpaintingRequest;
import org.makery.dto.InpaintingResponse;
import org.makery.repository.AiInpaintedDesignRepository; // 리포지토리명 변경 반영
import org.makery.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiInpaintedDesignService {

    private final AiInpaintedDesignRepository aiInpaintedDesignRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserService userService; // 유저 정보 조회를 위해 추가

    /**
     * 1. 인페인팅 생성 로직 (앨범 자산화)
     */
    @Transactional
    public InpaintingResponse generateInpainting(Long portfolioId, InpaintingRequest request, Long userId) {
        // 1-1. 원본 포트폴리오 조회 및 검증
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 포트폴리오입니다."));

        if (!portfolio.isInpaintingAllowed()) {
            throw new IllegalStateException("해당 포트폴리오는 AI 수정을 허용하지 않습니다.");
        }

        User user = userService.findById(userId);

        // 1-2. AI 생성 결과 (실제로는 aiClient를 통해 가져온 S3 URL이 들어갑니다)
        String mockAiImageUrl = "https://example.com/generated-cake-" + System.currentTimeMillis() + ".jpg";

        // 1-3. 새 엔티티 구조에 맞게 빌더 수정
        AiInpaintedDesign aiInpaintedDesign = AiInpaintedDesign.builder()
                .user(user)
                .originPortfolio(portfolio) // .portfolio() -> .originPortfolio()
                .inpaintingPrompt(request.prompt()) // .prompt() -> .inpaintingPrompt()
                .beforeImageUrl(portfolio.getImageUrl()) // 원본 이미지 경로 명시
                .afterImageUrl(mockAiImageUrl) // .resultImageUrl() -> .afterImageUrl()
                .isStoredInAlbum(true)
                .build();

        AiInpaintedDesign savedDesign = aiInpaintedDesignRepository.save(aiInpaintedDesign);
        return InpaintingResponse.from(savedDesign);
    }

    /**
     * 2. 인페인팅 결과 상세 조회 로직
     */
    @Transactional(readOnly = true)
    public InpaintingResponse getInpaintingDetail(Long portfolioId, Long inpaintingId) {
        // 1. 해당 인페인팅 결과 조회 (바뀐 엔티티 클래스 적용)
        AiInpaintedDesign inpainting = aiInpaintedDesignRepository.findById(inpaintingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 인페인팅 결과를 찾을 수 없습니다."));

        // 2. 보안 체크: 요청한 포트폴리오와 실제 데이터의 연관성 확인
        if (!inpainting.getOriginPortfolio().getId().equals(portfolioId)) {
            throw new IllegalArgumentException("잘못된 접근입니다. 포트폴리오 정보가 일치하지 않습니다.");
        }

        return InpaintingResponse.from(inpainting);
    }
}