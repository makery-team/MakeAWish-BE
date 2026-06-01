package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.domain.Tag;
import org.makery.dto.PortfolioFeedResponse;
import org.makery.dto.PortfolioResponse;
import org.makery.service.LikeService;
import org.makery.service.PortfolioService;
import org.makery.service.TagService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
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

    // 💡 추가된 좋아요 비즈니스 로직 서비스
    private final LikeService likeService;

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

    // ---------------------------------------------------------
    // 💡 여기서부터 새로 추가된 좋아요(찜) 기능입니다.
    // ---------------------------------------------------------

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
}