package org.makery.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SocketHandler implements WebSocketHandler {

    private final Map<Long, Map<Long, WebSocketSession>> chatRooms = new ConcurrentHashMap<>();

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private org.makery.repository.UserRepository userRepository;

    @Autowired
    private org.makery.service.NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            Map<String, Object> attributes = session.getAttributes();
            Long roomNumber = (Long) attributes.get("roomNumber");
            Long userId = (Long) attributes.get("userId");

            if (roomNumber == null || userId == null) {
                log.error("roomNumber 또는 userId가 null입니다. 연결 종료");
                session.close(CloseStatus.BAD_DATA);
                return;
            }

            // ✅ 방 존재 여부 확인 (없으면 연결 끊기)
            boolean roomExists = chatRoomRepository.existsById(roomNumber);
            if (!roomExists) {
                log.warn("존재하지 않는 방 번호입니다: {}", roomNumber);
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("존재하지 않는 채팅방"));
                return;
            }

            // ✅ 방 존재 시 등록
            chatRooms
                    .computeIfAbsent(roomNumber, k -> new ConcurrentHashMap<>())
                    .put(userId, session);

            log.info("사용자 {}가 방 {}에 연결됨", userId, roomNumber);

        } catch (Exception e) {
            log.error("afterConnectionEstablished 오류", e);
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (Exception ignore) {}
        }
    }


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        Map<String, Object> attributes = session.getAttributes();
        Long roomNumber = (Long) attributes.get("roomNumber");
        Long userId = (Long) attributes.get("userId");

        String payload = message.getPayload().toString();
        log.info("수신된 메시지: {}", payload);

        try {
            ChatMessageRequestDto requestDto;

            // JSON인지 아닌지 간단 검사
            if (payload.trim().startsWith("{") && payload.trim().endsWith("}")) {
                // JSON이면 DTO로 변환
                requestDto = objectMapper.readValue(payload, ChatMessageRequestDto.class);
            } else {
                // 단순 텍스트면 DTO를 수동 생성 (userId, roomNumber는 세션에서)
                requestDto = ChatMessageRequestDto.builder()
                        .userId(userId)
                        .roomNumber(roomNumber)
                        .message(payload)
                        .build();
            }

            chatMessageService.save(requestDto);
            log.info("메시지 저장 완료: {}", requestDto.getMessage());

            // 저장 후 보내는 메시지는 JSON 문자열로 보내는 게 좋음
            String sendPayload = objectMapper.writeValueAsString(requestDto);

            Map<Long, WebSocketSession> room = chatRooms.get(roomNumber);
            if (room != null) {
                for (Map.Entry<Long, WebSocketSession> entry : room.entrySet()) {
                    WebSocketSession s = entry.getValue();
                    try {
                        if (s.isOpen() && !entry.getKey().equals(userId)) {
                            s.sendMessage(new TextMessage(sendPayload));
                        }
                    } catch (Exception e) {
                        log.error("메시지 전송 실패 - userId: {}", entry.getKey(), e);
                    }
                }
            }

            // 🌟 상대방이 같은 웹소켓 방에 들어와있지 않은 경우 SSE 알림 발송
            try {
                ChatRoom chatRoom = chatRoomRepository.findById(roomNumber).orElse(null);
                if (chatRoom != null && chatRoom.getUser() != null && chatRoom.getOther() != null) {
                    Long otherUserId = chatRoom.getUser().getId().equals(userId)
                            ? chatRoom.getOther().getId()
                            : chatRoom.getUser().getId();

                    boolean isOtherInRoom = room != null && room.containsKey(otherUserId);
                    if (!isOtherInRoom) {
                        userRepository.findById(otherUserId).ifPresent(targetUser -> {
                            userRepository.findById(userId).ifPresent(senderUser -> {
                                String senderName = senderUser.getName() != null && !senderUser.getName().isBlank()
                                        ? senderUser.getName()
                                        : (senderUser.getNickname() != null ? senderUser.getNickname() : "대화 상대");

                                notificationService.createNotification(
                                        targetUser,
                                        "새 메시지 도착",
                                        String.format("[%s] %s", senderName, requestDto.getMessage()),
                                        org.makery.domain.NotificationType.CHAT,
                                        roomNumber
                                );
                            });
                        });
                    }
                }
            } catch (Exception notifEx) {
                log.warn("채팅 알림 발송 실패: {}", notifEx.getMessage());
            }

        } catch (Exception e) {
            log.error("메시지 저장 실패", e);
        }
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("전송 중 오류 발생", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Map<String, Object> attributes = session.getAttributes();
        Long roomNumber = (Long) attributes.get("roomNumber");
        Long userId = (Long) attributes.get("userId");

        if (roomNumber != null && userId != null) {
            Map<Long, WebSocketSession> room = chatRooms.get(roomNumber);
            if (room != null) {
                room.remove(userId);
                if (room.isEmpty()) {
                    chatRooms.remove(roomNumber);
                }
            }
        }

        log.info("사용자 {}가 방 {}에서 연결 종료", userId, roomNumber);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
