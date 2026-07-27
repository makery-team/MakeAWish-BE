# 🚨 AWS 실서버 배포 장애 최종 분석 및 포스트모텀 (Post-mortem) 리포트

이 문서는 MakeAWish 백엔드 서버가 AWS Elastic Beanstalk (RDS MySQL 8.0) 실서버 배포 과정에서 반복적으로 겪었던 **502 Bad Gateway (서버 구동 실패)** 및 **500/403 Internal Server Error (API 호출 실패)** 장애의 모든 과정과 근본 원인, 그리고 **AI 어시스턴트가 발견하지 못했던 결정적 원인(`SecurityConfig.java`)**을 상세히 기록한 최종 리포트입니다.

---

## 1. 📋 사건 타임라인 및 전체 과정 요약

```
[1차 배포 실패] 502 Bad Gateway 발생 (서버 구동 자체 실패)
   ├─ 원인 ①: DatabaseFixConfig.java가 실서버 DB에 이미 존재하는 제약조건을 중복 추가 시도 -> 1061 예외 발생 -> Fail-fast로 스프링 부트 강제 종료
   └─ 원인 ②: data.sql에서 TRUNCATE TABLE 사용 -> AWS RDS MySQL 외래키 제약조건(1701 에러)으로 초기 데이터 주입 실패
       ▼
[1차 조치] DatabaseFixConfig.java 삭제 & data.sql DELETE FROM 복구
       ▼
[2차 배포 및 문제 발생] 서버 구동 성공(502 탈출), 그러나 일부 API 호출 시 500/403 에러 발생
       ▼
[결정적 원인 발견 - AI가 짚지 못한 핵심] SecurityConfig.java의 `.requestMatchers(toH2Console())`
   ├─ 현상: H2 데이터베이스가 비활성화된 프로덕션 MySQL 환경에서 H2 콘솔 요청 매처(toH2Console)가 활성화되어 보안 필터 체인 충돌 및 예외 유발
   └─ 해결: 백엔드 개발자팀에서 `//.requestMatchers(toH2Console())` 주석 처리 완료
       ▼
[최종 성공] AWS 실서버 배포 100% 정상 작동 완료 (commit `fcae54c` -> `b009766`)
```

---

## 2. 🔍 핵심 장애 원인 3가지 기술적 심층 분석 (Deep Dive)

### ① [1차 원인] `DatabaseFixConfig.java`의 중복 제약조건 실행과 Fail-fast 크래시
* **문제 상황:**
  * 서버 구동 시 `@PostConstruct` 애너테이션이 붙은 `DatabaseFixConfig.init()` 메서드가 실행되면서 아래 SQL을 강제 실행했습니다.
    ```sql
    ALTER TABLE payments ADD CONSTRAINT UK_payments_order_id UNIQUE (order_id);
    ALTER TABLE refresh_token ADD CONSTRAINT UK_rt_user_id UNIQUE (user_id);
    ALTER TABLE reviews ADD CONSTRAINT UK_reviews_order_id UNIQUE (order_id);
    ```
  * AWS RDS MySQL 실서버에는 이미 해당 인덱스와 제약조건이 존재했기 때문에 MySQL은 `Duplicate key name 'UK_payments_order_id'` (Error 1061) 예외를 발생시켰습니다.
* **왜 예외 처리가 작동하지 않았는가?**
  * Spring의 `JdbcTemplate.execute()`는 MySQL 예외를 `org.springframework.jdbc.BadSqlGrammarException`으로 감싸서(Wrapping) 던집니다.
  * 이때 `e.getMessage()`는 `"StatementCallback; bad SQL grammar [ALTER TABLE...]"` 형태만 반환하여 `"Duplicate"` 단어나 에러코드 `"1061"`을 포함하지 않습니다.
  * 결과적으로 `if (msg.contains("Duplicate"))` 분기를 타지 못하고 `else` 블록으로 빠져 `RuntimeException("Failed to add required DB constraint")`을 던졌고, 이는 **스프링 부트 컨텍스트 초기화를 즉시 중단시켜 Nginx 502 Bad Gateway 에러를 초래**했습니다.

---

### ② [2차 원인] `data.sql` 내 `TRUNCATE TABLE`과 AWS RDS 외래키 제약조건 충돌
* **문제 상황:**
  * 초기 데이터 주입 스크립트(`data.sql`)에서 테이블 초기화를 위해 `TRUNCATE TABLE order_items;` 등의 구문을 사용했습니다.
* **왜 AWS MySQL에서 문제가 되었는가?**
  * H2 인메모리 DB나 외래키 제약조건이 느슨한 개발 환경과 달리, AWS RDS MySQL 8.0은 테이블 간 외래키(Foreign Key) 참조 관계가 엄격히 활성화되어 있습니다.
  * MySQL에서 참조 중인 테이블에 `TRUNCATE TABLE`을 수행하면 `ERROR 1701 (42000): Cannot truncate a table referenced in a foreign key constraint` 에러가 발생합니다.
  * 이로 인해 `data.sql` 실행이 실패하거나 불완전하게 주입되는 문제가 발생했습니다.
* **해결 조치:**
  * `TRUNCATE TABLE` 문법을 정상 작동 시절의 표준 DML 문법인 `DELETE FROM`으로 전면 복구하여 안전하게 초기화되도록 수정했습니다.

---

### ③ [AI가 발견하지 못한 결정적 문제] `SecurityConfig.java`의 `toH2Console()` 충돌
* **문제 상황:**
  * `DatabaseFixConfig.java`를 삭제하고 `data.sql`을 고친 이후에도, AWS 프로덕션 배포 시 특정 API 엔드포인트에서 500/403 Internal Server Error가 발생했습니다.
  * **AI 어시스턴트는 이 단계에서 DB 쿼리나 컨트롤러 쪽에 집중하여 진짜 근본 원인인 보안 설정(`SecurityConfig.java`)을 빠르게 지목하지 못했습니다.**
* **기술적 원인 (`toH2Console()`):**
  ```java
  @Bean
  public WebSecurityCustomizer configure() {
      return (web) -> web.ignoring()
              //.requestMatchers(toH2Console()) // <-- 실서버 장애를 일으킨 핵심 주범!
              .requestMatchers("/img/**", "/css/**", "/js/**");
  }
  ```
  * Spring Security 6의 `PathRequest.toH2Console()`은 H2 데이터베이스 콘솔 서블릿 경로(`spring.h2.console.path`)를 동적으로 조회하여 RequestMatcher를 등록합니다.
  * **하지만 AWS 실서버 환경(`application.yml`)은 MySQL DataSource를 사용하며 `spring.h2.console.enabled: false`로 설정되어 있거나 H2 관련 빈이 활성화되지 않습니다.**
  * 이 비-H2 환경에서 `toH2Console()` 매처가 Security Filter Chain에 포함되면, 요청 매칭 과정에서 H2 경로 속성을 찾지 못하거나 보안 컨텍스트 평가 중 예외를 유발하여 정상적인 API 호출을 차단하고 500/403 에러를 발생시켰습니다.
* **해결 조치:**
  * 백엔드 팀 개발자분께서 문제의 핵심을 정확히 파악하여 `SecurityConfig.java`의 `.requestMatchers(toH2Console())`를 주석 처리함으로써 마침내 모든 배포 장애를 종결지었습니다.

---

## 3. 🛠️ 최종 해결 커밋 내역 (`main` 브랜치)

`main` 브랜치에 반영된 주요 커밋 내역과 수정 파일은 다음과 같습니다:

1. **`fcae54c` - refactor: update data.sql with DELETE statements and remove DatabaseFixConfig**
   * **`src/main/java/org/makery/config/DatabaseFixConfig.java` (삭제):** 서버 구동을 멈추던 수동 스키마/제약조건 실행 및 fail-fast 시한폭탄 파일 전면 제거. (JPA `ddl-auto: update`로 스키마 관리 일임)
   * **`src/main/resources/data.sql` (수정):** `TRUNCATE TABLE` -> `DELETE FROM` 변환으로 외래키 충돌 방지 및 안전한 데이터 클렌징.
   * **`src/main/java/org/makery/config/SecurityConfig.java` (수정):** 비-H2 실서버 환경에서 예외를 유발하던 `//.requestMatchers(toH2Console())` 주석 처리.
2. **`6ec4877` - feat: add missing owner status update endpoint and dto for Heo Ye-jin**
   * 누락된 사장님 상태 업데이트 API 및 DTO 추가 보완.
3. **`b009766` - feat: merge and complement all 6 backend owner features for Heo Ye-jin**
   * 백엔드 6개 핵심 사장님(Owner) 기능 병합 및 실서버 정상 동작 최종 검증 완료.

---

## 4. 💡 향후 재발 방지를 위한 교훈 (Lessons Learned)

1. **프로덕션(RDS MySQL)과 개발(H2) 환경의 Security 설정 분리**
   * `toH2Console()`과 같은 특정 인메모리 DB 전용 설정은 프로덕션 `SecurityConfig`에 노출되지 않도록 프로필(`@Profile("local")` 등)로 철저히 분리해야 합니다.
2. **스프링 부트 구동 시점의 수동 DDL(`ALTER TABLE`) 강제 실행 지양**
   * 스키마 검증이나 수정은 Spring Data JPA(`ddl-auto`) 또는 검증된 마이그레이션 도구(Flyway, Liquibase)를 통과하게 하여 애플리케이션 시작을 예기치 않게 중단시키는 fail-fast 로직을 피해야 합니다.
3. **AI 어시스턴트의 한계 보완 및 근본 원인(Root Cause) 중심 접근**
   * 장애 발생 시 표면적인 쿼리나 단일 파일에 매몰되지 않고, `SecurityConfig`, `Filter Chain`, `Environment Properties` 등 애플리케이션 전반의 컨텍스트를 균형 있게 분석하는 자세가 필요합니다.
