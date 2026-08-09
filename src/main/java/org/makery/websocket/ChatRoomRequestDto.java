package org.makery.websocket;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatRoomRequestDto {

    private Long userId;
    private Long storeId; // 매장 ID (프론트에서 전달)
}
