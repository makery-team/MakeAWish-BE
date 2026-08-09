package org.makery.websocket;

import lombok.Getter;

import java.util.List;

@Getter
public class ChatRoomWithMessagesDto {

    private final Long roomNumber;
    private final Long userId;
    private final Long otherId;
    private final String otherName;
    private final List<ChatMessageResponseDto> messages;

    public ChatRoomWithMessagesDto(ChatRoom chatRoom, List<ChatMessageResponseDto> messages, Long requestUserId) {
        this.roomNumber = chatRoom.getRoomNumber();
        if (chatRoom.getUser().getId().equals(requestUserId)) {
            this.userId = requestUserId;
            this.otherId = chatRoom.getOther().getId();
            this.otherName = chatRoom.getOther().getNickname() != null ? chatRoom.getOther().getNickname() : chatRoom.getOther().getName();
        } else {
            this.userId = requestUserId;
            this.otherId = chatRoom.getUser().getId();
            this.otherName = chatRoom.getUser().getNickname() != null ? chatRoom.getUser().getNickname() : chatRoom.getUser().getName();
        }
        this.messages = messages;
    }
}
