package org.makery.websocket;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatRoomRequestDto {

    private Long userId;
    private Long storeId; // 매장 ID (프론트에서 전달)
}
