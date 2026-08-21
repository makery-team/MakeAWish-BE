# 2026-08-21 AI 포트폴리오 추천 정렬 고도화 및 기본 주문서 양식 표준화

## 1. 개요 및 변경 사항
1. **포트폴리오 추천 랭킹 고도화 (`PortfolioRepository`, `SearchIntentHandler`)**:
   - 1순위: 태그 일치도 (`COUNT(t.id) DESC`)
   - 2순위: 인기도 / 좋아요 수 (`p.likeCount DESC`)
   - 3순위: 최신 등록순 (`p.createdAt DESC`)
   - 태그가 없거나 매칭 결과가 없을 때 전체 인기 포트폴리오 정렬(`findAllRanked`) 자동 폴백
2. **주문서 표준 5대 기본 양식 자동 세팅 (`OrderSchemaUtil`, `UserService`, `ProductService`)**:
   - 신규 사장님 회원가입 및 신규 메뉴(Product) 생성 시 표준 기본 양식(사이즈, 맛/시트, 레터링, 픽업일시, 요청사항) 기본 자동 세팅

---

## 2. 검증 결과
- `./gradlew compileJava` 빌드 성공 (0 errors).
