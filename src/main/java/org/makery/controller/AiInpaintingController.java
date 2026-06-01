package org.makery.controller;

import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.dto.AiWebhookRequest;
import org.makery.dto.InpaintingRequest;
import org.makery.dto.InpaintingResponse;
import org.makery.service.AiInpaintedDesignService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-agent")
@RequiredArgsConstructor
public class AiInpaintingController {

    private final AiInpaintedDesignService inpaintingService;

    // 1. 프론트엔드에서 사용자가 인페인팅 요청 (비동기 접수)
    @PostMapping("/inpaint/{portfolioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InpaintingResponse> requestInpainting(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable Long portfolioId,
            @RequestBody InpaintingRequest request) {

        // 202 ACCEPTED로 처리 중임을 프론트엔드에 명확히 알림
        InpaintingResponse response = inpaintingService.requestInpainting(
                portfolioId,
                request,
                principalDetails.user()
        );

        return ResponseEntity.accepted().body(response);
    }

    // 2. 특정 인페인팅 결과물 상세 조회
    @GetMapping("/inpaint/{portfolioId}/{inpaintingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InpaintingResponse> getInpaintingDetail(
            @PathVariable Long portfolioId,
            @PathVariable Long inpaintingId) {

        InpaintingResponse response = inpaintingService.getInpaintingDetail(portfolioId, inpaintingId);
        return ResponseEntity.ok(response);
    }

    // 3. AI 서버가 작업 완료 후 결과물을 전송하는 웹훅 엔드포인트
    @PostMapping("/webhook/inpaint")
    public ResponseEntity<Void> inpaintingWebhookCallback(@RequestBody AiWebhookRequest request) {

        inpaintingService.processWebhookCallback(
                request.task_id(),
                request.result_image(),
                request.status()
        );

        return ResponseEntity.ok().build();
    }
}