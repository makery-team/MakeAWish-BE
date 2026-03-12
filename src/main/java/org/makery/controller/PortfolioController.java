package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.dto.PortfolioResponse;
import org.makery.service.PortfolioService;
import org.springframework.data.domain.Pageable; // ✅ 이걸로 반드시 바꿔야 합니다!
import org.springframework.data.web.PageableDefault; // 페이징 기본값 설정을 위해 추가
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * 포트폴리오 검색 조회
     * GET /api/portfolios?query=...&tags=...&page=0&size=20
     */
    @GetMapping("/portfolios")
    public ResponseEntity<List<PortfolioResponse>> searchPortfolios(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "latest") String sort,
            @PageableDefault(size = 20) Pageable pageable) { // @PageableDefault로 기본 사이즈 지정 가능

        // 이제 에러 없이 서비스의 search 메서드를 호출할 수 있습니다.
        List<PortfolioResponse> results = portfolioService.search(query, tags, sort, pageable);
        return ResponseEntity.ok(results);
    }
}
