# 2026-08-21 주문 상세 및 요약 DTO 고객 정보(customerId, customerName, customerPhone) 추가 가이드

## 1. 개요
- 사장님이 주문 상세 화면에서 고객의 실명과 연락처를 확인하고, 1:1 채팅방(`roomNumber`)으로 바로 연결될 수 있도록 DTO에 고객 식별자 및 프로필 정보를 추가.

---

## 2. 주요 변경 사항 (`MakeAWish-BE`)
1. **`dto/OrderDetailResponse.java` & `dto/OrderSummaryResponse.java`**:
   - `Long customerId`, `String customerName`, `String customerPhone` 필드 추가
   - `order.getUser()` 기반으로 안전한 이름/닉네임 추출 및 바인딩
