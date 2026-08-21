# 2026-08-21 JWT Principal 권한 매핑 및 사장님 주문 조회 버그 수정

## 1. 문제 원인 분석
- **현상**: 소비자가 사장님 매장으로 케이크 주문을 정상 접수하여 소비자 주문 내역에는 나타나나, 사장님 앱(`MakeAWish-FE-Owner`)의 홈 브리핑 및 주문 관리(`/orders`) 화면에는 "총 0건"으로 조회되지 않는 문제 발생.
- **근본 원인**:
  1. `TokenProvider.java`의 `getAuthentication` 메서드에서 JWT 토큰을 바탕으로 `User` 객체를 빌드할 때 `.userRole(userRole)` 설정이 누락되어 `PrincipalDetails.user().getUserRole()`이 항상 `null`로 반환됨.
  2. `OrderService.getMyOrders`에서 `role == UserRole.ROLE_SELLER` 조건이 항상 `false`가 되어 구매자 주문 조회(`findAllByUserIdOrderByCreatedAtDesc`) 쿼리가 실행되어 0건이 반환됨.
  3. `today` 필터 시 AWS 서버/DB 타임존 불일치 및 대기 주문 누락 가능성.

---

## 2. 주요 해결 사항 (`MakeAWish-BE`)
1. **`TokenProvider.java`**:
   - `Claims.get("role")` 값을 파싱하여 `User.builder().userRole(userRole)`을 올바르게 설정하도록 수정.
2. **`OrderService.java`**:
   - `getMyOrders` 및 `getOrderDetail` 호출 시 DB의 실시간 회원 상태 및 `user.getSellerProfile() != null` 여부를 확인하여 권한을 정확히 판별.
   - 사장님일 경우 `orderRepository.findAllBySellerId(userId)`를 통해 매장의 모든 주문을 안정적으로 조회.
   - `date=today` 필터링 시 `ZoneId.of("Asia/Seoul")` 기준 한국 날짜를 적용하고, 대기 주문(`PENDING_QUOTE`) 및 당일 픽업/생성 주문을 모두 포괄하도록 개선.

---

## 3. 검증 결과
- `./gradlew compileJava` 빌드 성공 (BUILD SUCCESSFUL).
