package org.makery.websocket;

import lombok.RequiredArgsConstructor;
import org.makery.domain.PrincipalDetails;
import org.makery.domain.User;
import org.makery.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final org.makery.repository.StoreRepository storeRepository;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/chatting/room")
    public ResponseEntity<ChatRoomWithMessagesDto> makeRoom(@AuthenticationPrincipal PrincipalDetails principalUser,
                                                            @RequestBody ChatRoomRequestDto chatRoomRequestDto) {
        // 1. 현재 로그인 유저를 DB에서 다시 조회 (영속 상태 보장)
        User user = userService.findById(principalUser.user().getId());

        // 2. 상대 유저도 조회 (매장 ID를 통해 사장님 유저 찾기)
        org.makery.domain.Store store = storeRepository.findById(chatRoomRequestDto.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
        User other = store.getSellerProfile().getUser();

        Optional<ChatRoom> optionalChatRoom = chatRoomService.findByUserAndOther(user, other);

        if (optionalChatRoom.isPresent()) {
            ChatRoom foundChatRoom = optionalChatRoom.get();
            List<ChatMessageResponseDto> messages = chatMessageService.findMessages(foundChatRoom.getRoomNumber());
            return ResponseEntity.ok(new ChatRoomWithMessagesDto(foundChatRoom, messages, user.getId()));
        } else {
            ChatRoom newChatRoom = chatRoomService.createRoom(user, other);
            return ResponseEntity.ok(new ChatRoomWithMessagesDto(newChatRoom, new ArrayList<>(), user.getId()));
        }
    }


    // 사용자가 속한 채팅방 목록 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chatting/rooms")
    public ResponseEntity<List<ChatRoomWithMessagesDto>> findAll(@AuthenticationPrincipal PrincipalDetails principalUser) {
        // principalUser에서 ID 추출
        Long userId = principalUser.user().getId();

        List<ChatRoomWithMessagesDto> chatRooms = chatRoomService.findByUserId(userId);
        return ResponseEntity.ok(chatRooms);
    }

    // 특정 채팅방의 메시지 내역 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chatting/rooms/{roomNumber}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getChatHistory(@PathVariable Long roomNumber) {
        List<ChatMessageResponseDto> messages = chatMessageService.findMessages(roomNumber);
        return ResponseEntity.ok(messages);
    }

    //채팅방 삭제
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/chatting/rooms/{roomNumber}")
    public ResponseEntity<String> deleteRoom(@AuthenticationPrincipal PrincipalDetails principalUser, @PathVariable Long roomNumber) {
        // 영속화된 객체를 쓰기 위해 DB에서 다시 조회
        User user = userService.findById(principalUser.user().getId());

        chatRoomService.deleteRoom(user, roomNumber);
        return ResponseEntity.ok("채팅방이 삭제되었습니다.");
    }
}