package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.Tag;
import org.makery.dto.PortfolioFeedResponse;
import org.makery.dto.PortfolioResponse;
import org.makery.service.PortfolioService;
import org.makery.service.TagService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
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
}
