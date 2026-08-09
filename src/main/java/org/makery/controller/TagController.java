package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final PortfolioService portfolioService;

    @GetMapping("/trending")
    public ResponseEntity<List<String>> getTrendingTags(
            @RequestParam(name = "limit", defaultValue = "7") int limit) {
        return ResponseEntity.ok(portfolioService.getTrendingTags(limit));
    }
}
