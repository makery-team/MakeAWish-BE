# ⚙️ MakeAWish Backend (Core Orchestrator API Server)

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring%20Data%20JPA-Hibernate%206-59666C?style=for-the-badge&logo=hibernate&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-8.0%20(InnoDB)-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/WebSocket-STOMP-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/AWS-RDS%20%26%20S3-FF9900?style=for-the-badge&logo=amazon-aws&logoColor=white" />
</p>

MakeAWish 백엔드 API 서버 저장소입니다. Java 17 및 Spring Boot 3.2 기반으로 구축되었으며, 클라이언트(모바일 앱, 사장님 웹)와 AI 마이크로서비스 간의 데이터 흐름을 오케스트레이션하고, 토스페이먼츠 간편결제, 주문 상태 머신, STOMP 실시간 채팅, SSE 주문 알림을 처리합니다.

---

## 📑 목차 (Table of Contents)
1. [주요 비즈니스 로직](#1-주요-비즈니스-로직)
2. [기술 스택 및 아키텍처](#2-기술-스택-및-아키텍처)
3. [패키지 구조 및 역할](#3-패키지-구조-및-역할)
4. [핵심 엔지니어링 구현 상세](#4-핵심-엔지니어링-구현-상세)
5. [트러블슈팅 및 결함 해결 사례](#5-트러블슈팅-및-결함-해결-사례)
6. [환경 설정 및 실행 방법](#6-환경-설정-및-실행-방법)

---

## 1. 주요 비즈니스 로직

- **하이브리드 RDB + JSON 스키마 관리**: 정형 데이터(주문 상태, 결제 금액, 회원 정보)는 RDB 테이블로 관리하고, 매장별 가변 주문서 항목은 `custom_schema` 및 `order_options` JSON 컬럼으로 유연하게 저장.
- **하버사인(Haversine) 공간 검색 Native Query**: B-Tree 인덱스 기반의 네이티브 SQL 공식으로 사용자의 현재 GPS 반경 1~5km 이내 매장을 서브 밀리초 단위로 검색.
- **STOMP 기반 1:1 실시간 상담 채팅**: WebSocket 연결 및 인메모리 브로커(`/topic/chat.{roomId}`)를 통해 실시간 메시지 및 주문서 카드 송수신.
- **Server-Sent Events (SSE) 신규 주문 푸시**: 사장님 웹으로 신규 주문 건을 HTTP 단방향 스트리밍(`SseEmitter`)하여 실시간 대시보드 자동 갱신 지원.
- **토스페이먼츠 결제 승인 및 멱등성 보장**: 결제 승인 API 호출 시 금액 대사(Reconciliation) 및 분산 락을 통한 중복 결제 방어.

---

## 2. 기술 스택 및 아키텍처

- **Java 17 & Spring Boot 3.2.x**: 선언적 트랜잭션(`@Transactional`), Hibernate 6 JPA ORM 표준 준수.
- **Spring Security 6.x & Stateless JWT**: 무상태 아키텍처 기반의 토큰 인증 및 WebSocket Handshake 단계 사전 인증 필터 적용.
- **AWS RDS MySQL 8.0 (InnoDB)**: ACID 트랜잭션 보장 및 JSON 데이터 타입 지원.

---

## 3. 패키지 구조 및 역할

```text
MakeAWish-BE/
├── src/main/java/com/makery/makeawish/
│   ├── client/                         # 외부 마이크로서비스 연동 (FastAPI AI, Toss)
│   ├── config/                         # Security, WebSocket, WebMvc 글로벌 설정
│   ├── controller/                     # REST API 엔드포인트 계층
│   ├── domain/                         # JPA 엔티티 및 도메인 핵심 규칙 (User, Store, Order)
│   ├── repository/                     # JPA Repository (하버사인 쿼리, 락 쿼리)
│   ├── service/                        # 비즈니스 오케스트레이션 및 트랜잭션 계층
│   └── websocket/                      # 웹소켓 핸드셰이크 인터셉터 및 STOMP 컨트롤러
└── src/main/resources/
    └── application.yml                 # DB 커넥션 풀, JWT 비밀키, 외부 엔드포인트 설정
```

---

## 4. 핵심 엔지니어링 구현 상세

### 4.1 하이브리드 RDB + JSON 속성 변환기 (`JsonAttributeConverter.java`)
정형화된 핵심 식별자(ID, 결제금액, 주문상태)는 RDB 정규화 컬럼으로 보장하고, 매장마다 변동성이 큰 옵션 데이터는 JPA `AttributeConverter`를 통해 안전하게 JSON 문자열로 직렬화하여 저장합니다.

```java
// converter/JsonAttributeConverter.java 발췌: Jackson ObjectMapper 기반 안전 변환
@Converter
public class JsonAttributeConverter implements AttributeConverter<Map<String, Object>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) return "{}";
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 직렬화 오류: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 역직렬화 오류: " + e.getMessage());
        }
    }
}
```

### 4.2 하버사인 구면 삼각법 매장 반경 탐색 (`repository/StoreRepository.java`)
지구 곡률(반경 6,371km)을 반영하는 구면 삼각법 공식을 MySQL Native Query로 인라인 구현하여, 별도의 GIS 엔진 없이도 인덱스 기반으로 3km 반경 매장을 10ms 이내에 고속 필터링 및 거리순 정렬합니다.

```java
// repository/StoreRepository.java 발췌: 하버사인 거리 계산 및 정렬 Native Query
@Query(value = """
    SELECT s.*, 
        (
            6371 * acos(
                cos(radians(:userLat)) * cos(radians(s.latitude)) * 
                cos(radians(s.longitude) - radians(:userLng)) + 
                sin(radians(:userLat)) * sin(radians(s.latitude))
            )
        ) AS distance
    FROM stores s
    HAVING distance <= :radiusKm
    ORDER BY distance ASC
    LIMIT :limit
""", nativeQuery = true)
List<Store> findStoresWithinRadius(
    @Param("userLat") double userLat, 
    @Param("userLng") double userLng, 
    @Param("radiusKm") double radiusKm, 
    @Param("limit") int limit
);
```

### 4.3 WebSocket Handshake 단계의 JWT 사전 검증 (`WebSocketConfig.java`)
웹소켓 커넥션 수립 이후 매 STOMP 프레임마다 토큰을 검증하는 오버헤드를 방지하기 위해, HTTP Upgrade 핸드셰이크 관문에서 토큰의 유효성을 1회 선제 검증하여 비인가 연결을 원천 차단합니다.

---

## 5. 트러블슈팅 및 결함 해결 사례

| 문제 현상 | 원인 분석 | 해결 방법 |
| :--- | :--- | :--- |
| **AI 엔드포인트 Security 500 에러** | 서비스 간 내부 통신 경로(`/api/v1/ai/**`)가 Security 필터 체인에서 인가 누락 | SecurityConfig에 내부 통신 전용 인가 규칙 및 토큰 검증 필터 체인 명시 |
| **S3 Presigned URL 서명 유실** | URL 인코딩 과정에서 특수문자가 치환되어 S3 업로드 시 403 Forbidden 발생 | 서명 쿼리스트링 원본 바이너리를 보존하는 서명 파서 적용 |
| **태그 검색 시 단어 불일치 결함** | `#생화` 입력 시 `#꽃`, `#플라워` 태그 케이크가 검색되지 않는 문제 | 형태소 분석 기반 유의어 확장 파이프라인(`SearchIntentHandler`) 구축 |

---

## 6. 환경 설정 및 실행 방법

### 6.1 환경 변수 설정 (`application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/makeawish?useSSL=false&serverTimezone=Asia/Seoul
    username: root
    password: your_password
jwt:
  secret: your_jwt_secret_key_minimum_256_bits
toss:
  secret-key: test_sk_your_toss_secret_key
ai:
  base-url: http://localhost:8000
```

### 6.2 빌드 및 실행

```bash
# Gradle 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```
