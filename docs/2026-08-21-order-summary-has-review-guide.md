# 2026-08-21 주문 요약 DTO(OrderSummaryResponse) 리뷰 작성 여부(hasReview) 추가 가이드

## 1. 개요
- 소비자가 이미 리뷰를 작성한 주문에 대해 클라이언트에서 작성 완료 뱃지 표시 및 중복 작성을 방지할 수 있도록 `OrderSummaryResponse`에 `hasReview` 필드를 추가.

---

## 2. 주요 변경 사항 (`MakeAWish-BE`)
1. **`domain/Order.java`**:
   - `@OneToOne(mappedBy = "order") private Review review;` 연관관계 매핑.
2. **`dto/OrderSummaryResponse.java`**:
   - `boolean hasReview` 필드 추가 및 `order.getReview() != null` 매핑.
