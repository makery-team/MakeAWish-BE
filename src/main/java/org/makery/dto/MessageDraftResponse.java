package org.makery.dto;

import lombok.Builder;

@Builder
public record MessageDraftResponse(
        Long orderId,
        String orderNumber,
        String draftMessage
) {
}
