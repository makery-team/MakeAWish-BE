package org.makery.service;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Inpainting;
import org.makery.domain.Portfolio;
import org.makery.dto.InpaintingRequest;
import org.makery.dto.InpaintingResponse;
import org.makery.repository.InpaintingRepository;
import org.makery.repository.PortfolioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InpaintingService {

    private final InpaintingRepository inpaintingRepository;
    private final PortfolioRepository portfolioRepository;

    /**
     * 1. 인페인팅 생성 로직
     */
    @Transactional
    public InpaintingResponse generateInpainting(Long portfolioId, InpaintingRequest request, Long userId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 포트폴리오입니다."));

        if (!portfolio.isInpaintingAllowed()) {
            throw new IllegalStateException("해당 포트폴리오는 AI 수정을 허용하지 않습니다.");
        }

        String mockAiImageUrl = "https://example.com/generated-cake-" + System.currentTimeMillis() + ".jpg";

        Inpainting inpainting = Inpainting.builder()
                .portfolio(portfolio)
                .prompt(request.prompt())
                .resultImageUrl(mockAiImageUrl)
                .build();

        Inpainting savedInpainting = inpaintingRepository.save(inpainting);
        return InpaintingResponse.from(savedInpainting);
    }

    /**
     * 2. 인페인팅 결과 상세 조회 로직
     * 💡 새로 추가된 메서드입니다.
     */
    @Transactional(readOnly = true)
    public InpaintingResponse getInpaintingDetail(Long portfolioId, Long inpaintingId) {
        // 1. 해당 인페인팅 결과가 존재하는지 확인
        Inpainting inpainting = inpaintingRepository.findById(inpaintingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 인페인팅 결과를 찾을 수 없습니다."));

        // 2. 💡 보안 체크: 요청한 포트폴리오 ID와 실제 데이터의 포트폴리오 ID가 일치하는지 검증
        if (!inpainting.getPortfolio().getId().equals(portfolioId)) {
            throw new IllegalArgumentException("잘못된 접근입니다. 포트폴리오 정보가 일치하지 않습니다.");
        }

        return InpaintingResponse.from(inpainting);
    }
}