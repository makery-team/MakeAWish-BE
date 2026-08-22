# 2026-08-22 주문 거절(REJECTED) 및 거절 사유(rejectReason) 처리 가이드

## 1. 개요
- 사장님이 주문을 거절할 때 상태를 `REJECTED`로 설정하고 거절 사유(`rejectReason`)를 저장할 수 있도록 백엔드 도메인 및 API를 보완.
- `OrderStatus`에 `REJECTED` enum을 추가하고, 주문 상세 및 요약 DTO에 `rejectReason` 필드를 포함하여 사장님 및 고객 모두에게 거절 사유를 명확히 안내할 수 있도록 구현.

---

## 2. 주요 변경 사항 (`MakeAWish-BE`)
1. **`domain/OrderStatus.java`**:
   - `REJECTED` enum 추가
2. **`domain/Order.java`**:
   - `rejectReason` 컬럼 추가
   - `updateStatus(OrderStatus newStatus, String reason)` 메서드 구현
3. **`dto/OrderStatusUpdateRequest.java`**:
   - `reason`, `rejectReason` 필드 추가
4. **`dto/OrderDetailResponse.java` & `dto/OrderSummaryResponse.java`**:
   - `String rejectReason` 필드 추가
5. **`controller/OrderController.java` & `service/OrderService.java`**:
   - `PATCH /api/orders/{orderId}/status` 및 `PATCH /api/orders/{orderId}`에서 거절 사유 파라미터 수신 및 저장 지원
