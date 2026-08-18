# 2026-08-19 매장 프로필 응답 DTO(MyStoreResponse, StoreResponse) imageUrl 필드 추가

## 1. 개요
- **목적**: 사장님 매장 프로필 조회(`GET /api/stores/me`) 및 일반 매장 상세 조회(`GET /api/stores/{storeId}`) 시 S3에 저장된 매장 프로필 이미지 URL(`imageUrl`)을 클라이언트에 정상 반환하도록 DTO에 필드 및 매핑 추가

---

## 2. 변경 파일
- `org.makery.dto.MyStoreResponse.java`: `imageUrl` 필드 및 빌더 매핑 추가
- `org.makery.dto.StoreResponse.java`: `imageUrl` 필드 및 빌더 매핑 추가

---

## 3. 검증 결과
- `./gradlew compileJava` 빌드 성공 (0 errors).
