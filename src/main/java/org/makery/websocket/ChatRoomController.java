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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/chatting/room")
    public ResponseEntity<ChatRoomWithMessagesDto> makeRoom(@AuthenticationPrincipal PrincipalDetails principalUser,
                                                            @RequestBody ChatRoomRequestDto chatRoomRequestDto) {
        // 1. 현재 로그인 유저를 DB에서 다시 조회 (영속 상태 보장)
        User user = userService.findById(principalUser.getUser().getId());

        // 2. 상대 유저도 조회
        User other = userService.findById(chatRoomRequestDto.getOtherId());

        Optional<ChatRoom> optionalChatRoom = chatRoomService.findByUserAndOther(user, other);

        if (optionalChatRoom.isPresent()) {
            ChatRoom foundChatRoom = optionalChatRoom.get();
            List<ChatMessageResponseDto> messages = chatMessageService.findMessages(foundChatRoom.getRoomNumber());
            return ResponseEntity.ok(new ChatRoomWithMessagesDto(foundChatRoom, messages));
        } else {
            ChatRoom newChatRoom = chatRoomService.createRoom(user, other);
            return ResponseEntity.ok(new ChatRoomWithMessagesDto(newChatRoom, new ArrayList<>()));
        }
    }


    // 사용자가 속한 채팅방 목록 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chatting/rooms")
    public ResponseEntity<List<ChatRoomWithMessagesDto>> findAll(@AuthenticationPrincipal PrincipalDetails principalUser) {
        // principalUser에서 ID 추출
        Long userId = principalUser.getUser().getId();

        List<ChatRoomWithMessagesDto> chatRooms = chatRoomService.findByUserId(userId);
        return ResponseEntity.ok(chatRooms);
    }

    //채팅방 삭제
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/chatting/rooms/{roomNumber}")
    public ResponseEntity<String> deleteRoom(@AuthenticationPrincipal PrincipalDetails principalUser, @PathVariable Long roomNumber) {
        // 영속화된 객체를 쓰기 위해 DB에서 다시 조회
        User user = userService.findById(principalUser.getUser().getId());

        chatRoomService.deleteRoom(user, roomNumber);
        return ResponseEntity.ok("채팅방이 삭제되었습니다.");
    }
}