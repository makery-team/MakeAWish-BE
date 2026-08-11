# 2026-08-11 System Release & Architecture Updates (Backend)

이 문서는 최신 스프린트 주기에 따라 성공적으로 배포 및 머지된 MakeAWish-BE의 주요 아키텍처 변경 사항 및 신규 피처를 정리한 통합 시스템 릴리즈 명세서입니다.

## 1. 데이터베이스 및 엔티티(Entity) 확장 아키텍처

### Store Entity 
- **`keywords` 필드 도입**: 매장의 핵심 키워드(태그) 데이터를 적재하기 위한 필드가 추가되었습니다.
  - 연동 포인트: 사장님 앱 온보딩 및 매장 프로필 관리, AI 소개글 자동 생성 파이프라인의 핵심 프롬프트 데이터로 활용됩니다.
- **`imageUrl` 필드 추가**: 매장 대표 이미지 URL을 영속화하는 필드가 추가되었습니다.
  - 연동 포인트: 프론트엔드(소비자 앱) 매장 목록 및 상세 페이지 렌더링에 사용됩니다.

### User Entity 및 도메인 정합성
- **회원 탈퇴 라이프사이클 보완**: `UserService`의 회원 탈퇴 로직 실행 시, 연관된 `SellerProfile` 데이터가 함께 안전하게 제거되도록 의존성 관리 및 데이터 정합성 유지 로직이 강화되었습니다.

## 2. 신규 API 연동 및 DTO 설계

### 전국 행정구역(Region) API 신설 (`RegionController`)
프론트엔드의 하드코딩된 필터 의존성을 제거하고, 서버 주도의 데이터 제공을 위한 API가 구축되었습니다.
- **데이터 소스**: 내부 리소스(`src/main/resources/regions.json`)를 기반으로 한 고성능 인메모리 파싱.
- **신규 엔드포인트**:
  - `GET /api/regions/cities` : 시/도 목록 조회
  - `GET /api/regions/gu?city={city}` : 특정 시/도의 구/군 목록 조회
  - `GET /api/regions/dong?city={city}&gu={gu}` : 특정 구/군의 동 목록 조회

### AI 인페인팅(Inpainting) DTO 스펙 고도화
레퍼런스 이미지 기반의 맞춤형 인페인팅 생성을 위해 DTO 스펙이 확장되었습니다.
- **`InpaintingRequest` / `InpaintingAiAsyncRequest`**: 
  - `referenceImageUrl` 필드 스펙 추가.
  - `AiInpaintedDesignService` 내부의 AI 서버 통신 계층과 연동하여 사용자 맞춤형 레퍼런스를 모델에 전달하도록 파이프라인이 구축되었습니다.

## 3. 시스템 안정화 및 핫픽스

### Spring Boot 3.x 어노테이션 호환성 대응
- **이슈**: `RegionService` 등에서 초기화 데이터를 로드하기 위해 `@PostConstruct`를 사용할 때 패키지 충돌(javax) 발생.
- **해결**: Spring Boot 3.x 환경 스펙에 맞추어 모든 `PostConstruct` 임포트를 `jakarta.annotation.PostConstruct`로 일괄 마이그레이션하여 시스템 빌드 안정성을 확보했습니다.
