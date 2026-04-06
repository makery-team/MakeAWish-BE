package org.makery.websocket;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessageRequestDto {

    public enum MessageType{
        ENTER, TALK
    }

    private Long userId;
    private String message;
    private String imageUrl;
    private Long roomNumber;

    @Builder
    public ChatMessageRequestDto(Long userId, String message, String imageUrl, Long roomNumber) {
        this.message = message;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.roomNumber = roomNumber;

    }

    public ChatMessage toEntity() {
        return ChatMessage.builder()
                .userId(userId)
                .message(message)
                .imageUrl(imageUrl)
                .roomNumber(roomNumber)
                .build();
    }
}