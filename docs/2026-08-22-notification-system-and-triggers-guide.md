# 2026-08-22 백엔드 알림(Notification) 시스템 및 실시간 트리거 연동 가이드

## 1. 개요
- 주문(생성, 견적, 거절, 진행, 픽업준비, 완료), 결제(토스 결제 승인), 채팅(새 메시지 수신) 등 시스템 전반의 주요 상태 변화에 대해 실시간 SSE Push 및 DB 저장을 지원하는 통합 알림 시스템 구축.

---

## 2. 주요 변경 사항 (`MakeAWish-BE`)

### 1) 도메인 & DTO
- `domain/NotificationType.java`: `ORDER`, `PAYMENT`, `CHAT`, `AI_SKETCH`, `SYSTEM` enum 추가
- `domain/Notification.java`: `title`, `type`, `targetId`, `markAsRead()` 추가
- `dto/NotificationResponse.java`: 확장 필드 반영
- `repository/NotificationRepository.java`: `countByUserIdAndIsReadFalse`, `findAllByUserIdAndIsReadFalse` 쿼리 추가

### 2) 서비스 & 컨트롤러 API
- `service/NotificationService.java`:
  - `createNotification(User user, String title, String message, NotificationType type, Long targetId)`
  - `markAsRead(Long notificationId, Long userId)`
  - `markAllAsRead(Long userId)`
  - `getUnreadCount(Long userId)`
- `controller/NotificationController.java`:
  - `GET /api/notifications/subscribe` (SSE 스트림 연결)
  - `GET /api/notifications` (알림 목록 조회)
  - `GET /api/notifications/unread-count` (미확인 알림 수)
  - `PATCH /api/notifications/{id}/read` (단건 읽음)
  - `PATCH /api/notifications/read-all` (전체 읽음)

### 3) 실시간 알림 트리거 연동
- **`OrderService`**:
  - `createOrder` ➔ 사장님에게 **[새 주문 접수]** 실시간 알림
  - `updateOrderStatus` / `updateOrderStatusByBody` ➔ 고객에게 **[견적 도착]**, **[주문 거절]**, **[제작 시작]**, **[픽업 준비 완료]**, **[픽업 완료]** 실시간 알림
- **`ExtraFeeService`**:
  - `updateExtraFee` ➔ 고객에게 **[추가 금액 안내]** 실시간 알림
- **`PaymentService`**:
  - `confirmTossPayment` ➔ 사장님에게 **[결제 완료 안내]** 실시간 알림
- **`SocketHandler`**:
  - `handleMessage` ➔ 상대방이 채팅방에 부재 중일 때 **[새 메시지 도착]** 실시간 SSE 알림
