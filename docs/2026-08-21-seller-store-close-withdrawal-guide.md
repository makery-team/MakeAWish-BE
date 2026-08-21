# 2026-08-21 사장님 매장 해지 및 탈퇴 분리 (DELETE /api/stores/me)

## 1. 개요 및 배경
- **문제점**: 사장님 앱에서 회원 탈퇴 시 `DELETE /api/users/me`를 호출하여 동일한 소셜(구글) 계정의 소비자 앱 계정까지 함께 삭제되는 현상 발생
- **해결 방안**: 사장님 앱에서의 탈퇴는 **"매장 폐업/해지 및 판매자 권한(`ROLE_SELLER`) 해제"**(`DELETE /api/stores/me`)로 분리하고, 일반 구매자 계정(`ROLE_USER`) 및 소비자 앱 이용 내역은 안전하게 보존

---

## 2. 주요 변경 사항
1. `org.makery.repository.SellerProfileRepository`: 신규 생성
2. `org.makery.domain.User`: `unregisterSeller()` 메서드 추가 (SellerProfile 연관관계 해제 및 `ROLE_USER` 권한 변경)
3. `org.makery.service.StoreService`: `closeMyStore(Long userId)` 비즈니스 로직 추가
4. `org.makery.controller.StoreController`: `DELETE /api/stores/me` 엔드포인트 추가

---

## 3. 검증 결과
- `./gradlew compileJava` 빌드 성공 (0 errors).
