# 2026-08-22 사장님-고객 간 1:1 채팅방 개설 지원 가이드

## 1. 개요
- 기존에는 소비자 앱에서 매장 ID(`storeId`)로만 1:1 채팅방 생성이 가능했으나, 사장님 앱에서도 주문 고객의 ID(`userId`)를 전달하여 언제든지 고객과의 1:1 채팅방을 개설/조회할 수 있도록 `POST /chatting/room` API를 확장.

---

## 2. 주요 변경 사항 (`MakeAWish-BE`)
1. **`websocket/ChatRoomController.java`**:
   - `makeRoom()`에서 `chatRoomRequestDto.getUserId()`가 전달될 경우, 해당 고객 유저를 조회하여 사장님-고객 간의 1:1 채팅방을 조회 또는 신규 생성하여 반환.
