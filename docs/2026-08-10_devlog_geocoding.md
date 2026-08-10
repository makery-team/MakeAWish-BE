# [MakeAWish-BE] 매장 주소 지오코딩(Geocoding) 연동 (2026-08-10)

## 1. 개요
사장님 앱에서 매장 프로필 수정 시, 주소 텍스트만 전달되고 지도에 표시하기 위한 `latitude`와 `longitude`가 누락되는 문제를 백엔드에서 자체적으로 해결하기 위해 지오코딩 API를 연동했습니다.

## 2. 변경 내용

### 2.1 Kakao Local API 클라이언트 추가
- `org.makery.service.KakaoLocalClient` FeignClient 인터페이스 생성
- 카카오 주소 검색 API (`/v2/local/search/address.json`) 연동

### 2.2 StoreService 지오코딩 로직 주입
- `StoreService.updateStoreProfile` 메서드 호출 시, `latitude`와 `longitude`가 함께 오면 해당 값을 우선 저장.
- 없다면, 입력된 `address`를 바탕으로 카카오 로컬 API를 호출하여 위경도(`x`, `y`) 좌표를 추출하고 DB에 저장하도록 구현.
- 환경변수 `KAKAO_REST_API_KEY`를 바탕으로 카카오 인증 헤더 세팅.

### 2.3 StoreProfileUpdateRequest DTO 변경
- 차후 프론트엔드(사장님 앱)에서 좌표를 직접 선택할 수 있는 상황에 대비해 `latitude`, `longitude` 필드를 미리 개방해 둠.

## 3. 유의 사항
- 실서버 배포 시 반드시 `KAKAO_REST_API_KEY` 환경변수가 세팅되어 있어야 합니다.
- (누락 시 경고 로그만 남기고 좌표 저장을 스킵하며, 로직 에러로 실패하지 않도록 예외 처리 완료)
