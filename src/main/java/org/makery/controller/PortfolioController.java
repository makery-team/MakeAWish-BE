package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Tag;
import org.makery.dto.InpaintingRequest;
import org.makery.dto.InpaintingResponse;
import org.makery.dto.PortfolioFeedResponse;
import org.makery.dto.PortfolioResponse;
import org.makery.service.AiInpaintedDesignService;
import org.makery.service.PortfolioService;
import org.makery.service.TagService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final TagService tagService;
    private final AiInpaintedDesignService inpaintingService;

    /**
     * 1. 포트폴리오 검색 조회
     * GET /api/portfolios?query=...&tags=...
     */
    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getPortfolios(@RequestParam(required = false) String tags,
                                                                 @RequestParam(defaultValue = "latest") String sort,
                                                                 @PageableDefault(size = 10) Pageable pageable) {

        Set<Tag> tagEntities = null;
        if (tags != null && !tags.isBlank()) {
            Set<String> tagNames = Set.of(tags.split(","));
            tagEntities = tagService.findByNames(tagNames);
        }

        List<PortfolioResponse> results = portfolioService.search(tagEntities, sort, pageable);

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
     */
    @GetMapping("/{portfolioId}/inpaintings/{inpaintingId}")
    public ResponseEntity<InpaintingResponse> getInpaintingDetail(
            @PathVariable Long portfolioId,
            @PathVariable Long inpaintingId
    ) {
        InpaintingResponse response = inpaintingService.getInpaintingDetail(portfolioId, inpaintingId);
        return ResponseEntity.ok(response);
    }

    /**
     * 태그 기반 피드 조회 API
     * 예: /api/portfolios/search?tags=생일,레터링,초코
     */
    @GetMapping("/feeds")
    public ResponseEntity<Slice<PortfolioFeedResponse>> searchFeedByTags(
            @RequestParam(name = "tags", required = false) List<String> tags,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(portfolioService.searchFeedByTags(tags, pageable));
    }
}