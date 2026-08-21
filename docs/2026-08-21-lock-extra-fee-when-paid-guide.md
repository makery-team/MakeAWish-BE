# 2026-08-21 결제 완료/진행 중 주문 추가금 수정 방지 검증 가이드

## 1. 개요
- 결제가 이미 완료되었거나(`PAID`), 제작 진행(`IN_PROGRESS`), 픽업 대기/완료(`PICKUP_READY`, `COMPLETED`) 상태인 주문은 추가금 변경 및 추가가 불가능하도록 백엔드 비즈니스 로직에 검증을 추가.

---

## 2. 주요 변경 사항 (`MakeAWish-BE`)
1. **`service/ExtraFeeService.java`**:
   - `order.getStatus()`가 `PENDING_QUOTE` 또는 `QUOTED`가 아닐 경우 `IllegalStateException("결제가 완료되었거나 진행 중인 주문은 추가금을 수정할 수 없습니다.")` 예외 발생
