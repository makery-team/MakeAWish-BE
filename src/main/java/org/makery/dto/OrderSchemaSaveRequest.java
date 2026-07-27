package org.makery.dto;

import java.util.Map;

public record OrderSchemaSaveRequest(
        Long productId,
        Map<String, Object> orderSchema
) {
}
