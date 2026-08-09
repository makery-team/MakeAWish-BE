# 🛠 트러블슈팅 리포트: 매장 개설 500 / 400 에러 해결

**작성일**: 2026-08-09
**대상**: MakeAWish-BE (Spring Boot 백엔드)

## 1. 매장 프로필 생성/수정 시 500 Data Truncation 에러
- **증상**: 프론트엔드에서 `PATCH /api/stores/profile` 호출 시, 400자가 넘는 `businessHours` JSON 배열 데이터를 전송하면 AWS RDS 환경에서 500 에러(Data too long for column) 발생.
- **원인**: 
  - 엔티티에서 `@Column(columnDefinition = "TEXT")`를 추가했으나, `spring.jpa.hibernate.ddl-auto=update` 옵션은 기존에 이미 `VARCHAR(255)`로 생성되어 있는 컬럼의 데이터 타입을 강제로 변경하지 않음 (데이터 유실 방지 정책).
  - 결국 코드는 `TEXT`로 배포되었지만 실제 DB는 여전히 `VARCHAR` 상태로 남아있어 데이터가 잘림.
- **해결**:
  - `Store.java`에서 해당 필드의 컬럼 매핑 이름을 아예 새로 지정(`@Column(name = "hours_json", columnDefinition = "TEXT")`).
  - 하이버네이트가 이를 완전히 새로운 컬럼으로 인식하게 하여 DB에 완벽한 `TEXT` 컬럼을 새로 생성하도록 유도함.

## 2. 매장 개설 초기화(init) API 400 Bad Request 에러
- **증상**: 프론트엔드에서 `PATCH /api/users/me/init` 호출 시 명확한 에러 메시지 없이 400 Bad Request 응답 반환.
- **원인 1 (Jackson Record 매핑 버그)**: 
  - `UserProfileInitRequest`가 Java 17 `record`로 선언되어 있었음. 
  - 프론트엔드에서 `{"isSeller": true}`를 전송했으나, Jackson 라이브러리가 boolean 타입 필드명(`isSeller`)의 getter를 추론할 때 `is`를 떼버리고 `seller`라는 키값을 기대하는 고질적인 매핑 오류 발생. (이로 인해 파싱에 실패하고 `HttpMessageNotReadableException` 발생 → 400 에러)
- **원인 2 (중복 닉네임 예외 처리 누락)**: 
  - 테스트 중 온보딩 상태를 초기화하고 동일한 닉네임("메이크어위시")으로 재가입을 시도할 경우, 백엔드에서 `IllegalStateException("이미 사용 중인 닉네임입니다.")`을 던짐.
  - 하지만 `GlobalExceptionHandler`에 해당 예외 핸들러가 누락되어 있어, 프론트엔드가 정확한 JSON 메시지 대신 알 수 없는 400 에러를 받게 됨.
- **해결**:
  - `UserProfileInitRequest.java`의 `isSeller` 필드에 `@JsonProperty("isSeller")` 어노테이션을 명시적으로 추가하여 파싱 규칙을 강제함.
  - `GlobalExceptionHandler.java`에 `@ExceptionHandler(IllegalStateException.class)`를 추가하여, 중복 닉네임 예외 발생 시 `{"error": "BAD_REQUEST", "message": "이미 사용 중인 닉네임입니다."}` 형태로 명확한 에러를 반환하도록 수정함.
