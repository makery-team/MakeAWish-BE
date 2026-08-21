# 2026-08-21 주문 상세 및 요약 DTO 추가금(extraFee) 필드 노출 가이드

## 1. 개요
- 소비자가 사장님이 책정한 추가 금액(`extraFee`)과 추가금 사유(`extraFeeReason`)를 주문 목록 및 상세 조회 화면에서 명확하게 확인할 수 있도록 DTO에 필드를 추가.

---

## 2. 주요 변경 사항 (`MakeAWish-BE`)
1. **`dto/OrderDetailResponse.java`**:
   - `Integer extraFee`, `String extraFeeReason`, `boolean hasReview` 필드 추가
   - `order.getExtraFee() != null ? order.getExtraFee() : 0`, `order.getExtraFeeReason()`, `order.getReview() != null` 매핑
2. **`dto/OrderSummaryResponse.java`**:
   - `Integer extraFee`, `String extraFeeReason` 필드 추가
