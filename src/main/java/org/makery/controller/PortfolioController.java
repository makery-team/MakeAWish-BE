package org.makery.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.domain.Tag;
import org.makery.domain.User;
import org.makery.dto.*;
import org.makery.service.LikeService;
import org.makery.service.PortfolioService;
import org.makery.service.TagService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final TagService tagService;
    private final LikeService likeService;

    // ==========================================
    // [사용자 기능]
    // ==========================================

    // 1. 포트폴리오 기본 조회 및 태그 검색
    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getPortfolios(
            @RequestParam(required = false) String tags,
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

    // 2. 메인 피드용 포트폴리오 무한 스크롤 조회
    @GetMapping("/feeds")
    public ResponseEntity<Slice<PortfolioFeedResponse>> searchFeedByTags(
            @RequestParam(name = "tags", required = false) List<String> tags,
            @PageableDefault(size = 12) Pageable pageable) {

        return ResponseEntity.ok(portfolioService.searchFeedByTags(tags, pageable));
    }

    // 3. 포트폴리오 좋아요 추가
    @PostMapping("/{portfolioId}/likes")
    public ResponseEntity<Void> addLike(
            @PathVariable("portfolioId") Long portfolioId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        // 💡 User() 가 아니라 소문자 user() 입니다!
        likeService.addLike(principalDetails.user().getId(), portfolioId);
        return ResponseEntity.ok().build();
    }

    // 4. 포트폴리오 좋아요 취소
    @DeleteMapping("/{portfolioId}/likes")
    public ResponseEntity<Void> removeLike(
            @PathVariable("portfolioId") Long portfolioId,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        // 💡 User() 가 아니라 소문자 user() 입니다!
        likeService.removeLike(principalDetails.user().getId(), portfolioId);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // [사장님(Partner) 기능]
    // ==========================================

    /**
     * 등록 전 포트폴리오 태그 추천 API
     * POST /api/portfolios/tags/recommend
     */
    @PostMapping("/tags/recommend")
    public ResponseEntity<List<String>> recommendPortfolioTags(
            @Valid @RequestBody PortfolioTagRecommendRequest request) {

        List<String> recommendedTags = portfolioService.recommendTagsWithAi(request);
        return ResponseEntity.ok(recommendedTags);
    }

    /**
     * 포트폴리오 신규 등록
     * POST /api/portfolios
     */
    @PostMapping
    public ResponseEntity<PortfolioResponse> registerPortfolio(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @Valid @RequestBody PortfolioRegisterRequest request) {

        User currentSeller = principalDetails.user();
        PortfolioResponse response = portfolioService.registerPortfolio(currentSeller, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 포트폴리오 정보 수정
     * PATCH /api/portfolios/{portfolioId}
     */
    @PatchMapping("/{portfolioId}")
    public ResponseEntity<Void> updatePortfolio(
            @PathVariable("portfolioId") Long portfolioId,
            @RequestBody PortfolioUpdateRequest request) {

        portfolioService.updatePortfolio(portfolioId, request);
        return ResponseEntity.ok().build();
    }
}