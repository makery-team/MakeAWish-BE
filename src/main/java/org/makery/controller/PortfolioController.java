package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.dto.InpaintingRequest;
import org.makery.dto.InpaintingResponse;
import org.makery.dto.PortfolioResponse;
import org.makery.service.InpaintingService;
import org.makery.service.PortfolioService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final InpaintingService inpaintingService;

    /**
     * 1. 포트폴리오 검색 조회
     * GET /api/portfolios?query=...&tags=...
     */
    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getPortfolios(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tags,
            @RequestParam(defaultValue = "latest") String sort,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        List<PortfolioResponse> results = portfolioService.search(query, tags, sort, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * 2. 포트폴리오 기반 인페인팅 생성 API
     * POST /api/portfolios/{portfolioId}/inpaintings
     */
    @PostMapping("/{portfolioId}/inpaintings")
    public ResponseEntity<InpaintingResponse> createInpainting(
            @PathVariable Long portfolioId,
            @RequestBody InpaintingRequest request
    ) {
        Long userId = 1L; // 테스트용 임시 ID
        InpaintingResponse response = inpaintingService.generateInpainting(portfolioId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 3. 인페인팅 개별 결과 상세 조회 API
     * GET /api/portfolios/{portfolioId}/inpaintings/{inpaintingId}
     * 💡 새로 추가된 메서드입니다!
     */
    @GetMapping("/{portfolioId}/inpaintings/{inpaintingId}")
    public ResponseEntity<InpaintingResponse> getInpaintingDetail(
            @PathVariable Long portfolioId,
            @PathVariable Long inpaintingId
    ) {
        InpaintingResponse response = inpaintingService.getInpaintingDetail(portfolioId, inpaintingId);
        return ResponseEntity.ok(response);
    }
}