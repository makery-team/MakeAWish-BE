# 2026-08-22 백엔드 스마트폰 OS 백그라운드 푸시 및 알림 수신설정 가이드

## 1. 개요
- 스마트폰 화면이 꺼져있거나 앱이 백그라운드에 있을 때도 주문/결제/채팅 알림을 실시간으로 수신할 수 있도록 **Expo Push Server API** 기반의 비동기 백그라운드 푸시 시스템을 구축하고, 유저별 **알림 수신동의(주문/채팅/마케팅) 및 디바이스 토큰 관리 API**를 구현.

---

## 2. 주요 변경 사항 (`MakeAWish-BE`)

### 1) 도메인 & 엔티티
- `User.java`: `orderPushEnabled`(기본 true), `chatPushEnabled`(기본 true), `marketingPushEnabled`(기본 false) 필드 및 수정 메서드 추가
- `DeviceToken.java`: 유저별 디바이스 푸시 토큰(`token`, `platform`, `lastActiveAt`) 엔티티 신설
- `DeviceTokenRepository.java`: 토큰 기반 조회/삭제 및 유저별 토큰 조회 쿼리 구현

### 2) DTO
- `DeviceTokenRequest.java`: 디바이스 토큰 등록/삭제 요청
- `NotificationSettingsResponse.java`: 알림 수신 설정 응답
- `NotificationSettingsUpdateRequest.java`: 알림 수신 설정 변경 요청

### 3) 서비스
- `PushNotificationService.java`: Expo Push API(`https://exp.host/--/api/v2/push/send`)를 통한 비동기(`@Async`) 멀티 플랫폼 푸시 발송
- `NotificationService.java`:
  - 알림 생성(`createNotification`) 시 사용자의 수신 동의 여부 검증 후 등록된 디바이스로 OS 백그라운드 푸시 자동 발송
  - `registerDeviceToken`, `removeDeviceToken`, `getNotificationSettings`, `updateNotificationSettings` 구현

### 4) 컨트롤러 엔드포인트 (`NotificationController`)
- `POST /api/notifications/device-token` : 디바이스 푸시 토큰 등록/갱신
- `DELETE /api/notifications/device-token` : 디바이스 푸시 토큰 삭제 (로그아웃 시)
- `GET /api/notifications/settings` : 알림 수신 설정 조회
- `PATCH /api/notifications/settings` : 알림 수신 설정 변경
