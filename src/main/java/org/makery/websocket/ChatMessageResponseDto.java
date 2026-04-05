package org.makery.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ChatMessageResponseDto {

    private Long userId;
    private String message;
    private String imageUrl;
    private Long roomNumber;
    private LocalDateTime createdTime;

    public ChatMessageResponseDto(ChatMessage chatMessage) {
        this.userId = chatMessage.getUserId();
        this.message = chatMessage.getMessage();
        this.imageUrl = chatMessage.getImageUrl();
        this.roomNumber = chatMessage.getRoomNumber();
        this.createdTime = chatMessage.getCreatedTime();
    }
}