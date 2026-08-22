# 2026-08-22 구글 OAuth 토큰 검증(GoogleTokenVerifier) 멀티 플랫폼 클라이언트 ID 및 Fallback 지원 가이드

## 1. 개요
- Google OAuth 2.0은 플랫폼(웹, Android, iOS)별로 발급되는 ID Token의 `aud`(Audience) 대상 값이 각기 다릅니다.
- 기존 백엔드에서는 `app.auth.google.web-client-id` 단일 대상만 검증하도록 되어 있었고, 환경변수 누락 시 빈 문자열이 주입되어 웹/앱 모든 구글 토큰 검증이 실패하는 문제가 있었습니다.

---

## 2. 주요 변경 사항 (`MakeAWish-BE`)

### 1) GoogleTokenVerifier 멀티 클라이언트 ID 지원
- `KNOWN_CLIENT_IDS` 등록:
  - Web: `106131390766-mnqk6vkbs4n33s2tt63om1860e6cgaau.apps.googleusercontent.com`
  - Android: `106131390766-5tiajbml0itkiohc580sm5mc3t3tiahb.apps.googleusercontent.com`
  - iOS: `106131390766-vmcvo280rnguao23e9bkmo76d4fnd850.apps.googleusercontent.com`
- `application.yml`: `web-client-id` 기본 fallback 값 명시
- 유효한 모든 플랫폼의 Google ID Token을 Google 공개키 기반으로 정상 검증 및 로그인 처리
