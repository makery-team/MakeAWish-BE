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

    public ChatRoomWithMessagesDto(ChatRoom chatRoom, List<ChatMessageResponseDto> messages) {
        this.roomNumber = chatRoom.getRoomNumber();
        this.userId = chatRoom.getUser().getId();
        this.otherId = chatRoom.getOther().getId();
        this.otherName = chatRoom.getOther().getNickname() != null ? chatRoom.getOther().getNickname() : chatRoom.getOther().getName();
        this.messages = messages;
    }
}
