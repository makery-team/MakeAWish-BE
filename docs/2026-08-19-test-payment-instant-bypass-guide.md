# 2026-08-19 테스트 결제 바이패스(Test Bypass) 지원 가이드

## 1. 개요
- **목적**: 모바일 에뮬레이터나 실기기 테스트 환경에서 실제 카드사/토스/카카오페이 앱이 미설치되어 있어도, 번거로운 SMS/본인인증 없이 원클릭으로 결제 승인(`PAID`) 상태로 전환할 수 있도록 테스트 바이패스 키(`test_bypass_...`)를 지원합니다.

---

## 2. 주요 구현 사항
- **`org.makery.service.PaymentService.java`**:
  - `request.paymentKey()`가 `test_bypass_`로 시작하는 경우, PG사 외부 승인 API 통신 대신 로컬 데이터베이스 결제 완료(`PAID`) 트랜잭션을 즉시 수행하고 반환합니다.

---

## 3. 검증 결과
- `./gradlew compileJava` 빌드 성공 (0 errors).
