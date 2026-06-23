package org.makery.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.makery.domain.AiInpaintedDesign;
import org.makery.domain.InpaintingStatus;
import org.makery.domain.Portfolio;
import org.makery.domain.User;
import org.makery.dto.InpaintingAiAsyncRequest;
import org.makery.dto.InpaintingRequest;
import org.makery.dto.InpaintingResponse;
import org.makery.repository.AiInpaintedDesignRepository;
import org.makery.repository.PortfolioRepository;
import org.makery.websocket.AwsS3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInpaintedDesignService {

    private final AiClient aiClient;
    private final AwsS3Service awsS3Service;
    private final PortfolioRepository portfolioRepository;
    private final AiInpaintedDesignRepository aiInpaintedDesignRepository;

    @Value("${app.server.base-url}")
    private String backendBaseUrl;

    /**
     * 1. 사용자로부터 인페인팅 요청을 접수 (비동기)
     */
    @Transactional
    public InpaintingResponse requestInpainting(Long portfolioId, InpaintingRequest request, User user) {
        Portfolio origin = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new EntityNotFoundException("원본 디자인을 찾을 수 없습니다."));

        String cleanMaskB64 = extractPureBase64(request.maskImage());

        // 연속 편집을 위한 현재 이미지 판단 (URL 또는 Base64)
        String imageUrl = null;
        String imageB64 = null;
        
        if (request.currentImage() != null && !request.currentImage().isBlank()) {
            if (request.currentImage().startsWith("http")) {
                imageUrl = request.currentImage().split("\\?")[0]; // 캐시 무효화 쿼리스트링 제거
            } else {
                imageB64 = extractPureBase64(request.currentImage());
            }
        } else {
            imageUrl = origin.getImageUrl();
        }

        // DB에 저장할 'beforeImageUrl' 처리
        String beforeImageForDb = (imageUrl != null) ? imageUrl : "BASE64_EDITED_IMAGE";

        // 1. PENDING 상태로 DB에 우선 저장 (비동기 콜백 추적용 ID 발급)
        AiInpaintedDesign pendingDesign = aiInpaintedDesignRepository.save(AiInpaintedDesign.builder()
                .user(user)
                .originPortfolio(origin)
                .inpaintingPrompt(request.prompt())
                .beforeImageUrl(beforeImageForDb)
                .status(InpaintingStatus.PENDING)
                .build());

        // 2. AI 서버에 전송할 비동기 연동 DTO 생성 (웹훅 도메인 조립)
        String webhookUrl = backendBaseUrl + "/api/ai-agent/webhook/inpaint";
        InpaintingAiAsyncRequest aiRequest = new InpaintingAiAsyncRequest(
                request.prompt(),
                imageUrl,
                imageB64,
                cleanMaskB64,
                pendingDesign.getId(),
                webhookUrl
        );

        // 3. OpenFeign 클라이언트를 통해 외부 AI 엔진에 태스크 위임
        try {
            aiClient.requestInpaintedImageAsync(aiRequest);
        } catch (Exception e) {
            log.error("AI 서버로 비동기 인페인팅 요청 전송 실패. TaskID: {}", pendingDesign.getId(), e);
            pendingDesign.updateFailed();
            throw new RuntimeException("AI 서버 통신 장애로 작업을 지시하지 못했습니다.", e);
        }

        return InpaintingResponse.from(pendingDesign);
    }

    /**
     * 2. AI 서버가 작업 완료 후 웹훅을 호출했을 때 처리 (사후 파일 가공 및 상태 확정)
     */
    @Transactional
    public void processWebhookCallback(Long taskId, String resultBase64, String aiStatus) {
        AiInpaintedDesign pendingDesign = aiInpaintedDesignRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("해당 작업 ID를 찾을 수 없습니다: " + taskId));

        // [중복 콜백 방어] 이미 완료되었거나 실패한 작업인 경우 중복 처리 흐름을 차단 (멱등성 보장)
        if (pendingDesign.getStatus() != InpaintingStatus.PENDING) {
            log.warn("이미 처리가 완료되었거나 실패 상태로 종결된 태스크입니다. 작업을 무시합니다. TaskID: {}", taskId);
            return;
        }

        // AI 서버 자체 처리 실패 응답 또는 데이터 누락
        if (!"COMPLETED".equalsIgnoreCase(aiStatus) || resultBase64 == null || resultBase64.isBlank()) {
            log.error("AI 서버 인페인팅 실패 웹훅 수신 또는 파일 데이터 누락. TaskID: {}", taskId);
            pendingDesign.updateFailed();
            return;
        }

        try {
            // Python 서버에서 이미 S3에 업로드 후 URL을 넘겨주므로, 재업로드 과정 생략하고 바로 저장
            String permanentUrl = resultBase64;

            // 데이터베이스 엔티티 상태를 COMPLETED로 변경 및 영구 보관 URL 세팅 (Dirty Checking)
            pendingDesign.updateComplete(permanentUrl);

            log.info("인페인팅 비동기 웹훅 파이프라인 처리 완료. DB 저장 성공. TaskID: {}", taskId);

            // TODO: [선택 사항] WebSocket 기반 클라이언트 Push 또는 SSE 응답 세션을 이곳에서 호출

        } catch (Exception e) {
            log.error("웹훅 콜백 사후 가공 처리 중 예외 발생. 트랜잭션 롤백 및 실패 처리를 강제합니다. TaskID: {}", taskId, e);
            pendingDesign.updateFailed();

            // 🌟 중요: 런타임 예외를 상위 컨테이너로 리플로우하여 스프링 프레임워크가 정상 롤백을 수행하도록 제어
            throw new RuntimeException("비동기 웹훅 이미지 파싱 및 적재 중 내부 시스템 오류 발생", e);
        }
    }

    /**
     * 프론트엔드로부터 유입된 Data URL 프리픽스를 절삭하고 순수 인코딩 스트림만 반환
     */
    private String extractPureBase64(String dataUrl) {
        if (dataUrl == null) return null;
        if (dataUrl.contains(",")) return dataUrl.split(",")[1];
        return dataUrl;
    }

    /**
     * 특정 인페인팅 세션 상세 데이터 검증 조회
     */
    @Transactional(readOnly = true)
    public InpaintingResponse getInpaintingDetail(Long portfolioId, Long inpaintingId) {
        AiInpaintedDesign inpainting = aiInpaintedDesignRepository.findById(inpaintingId)
                .orElseThrow(() -> new EntityNotFoundException("해당 이미지 수정 결과를 찾을 수 없습니다."));

        // 유입 경로와 원본 대상 데이터의 소유 도메인 일치성 방어 검증
        if (inpainting.getOriginPortfolio() != null &&
                !inpainting.getOriginPortfolio().getId().equals(portfolioId)) {
            throw new IllegalArgumentException("잘못된 접근입니다. 원본 포트폴리오 자원 정보가 일치하지 않습니다.");
        }

        return InpaintingResponse.from(inpainting);
    }
}