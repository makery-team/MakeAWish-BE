package org.makery.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record OrderCreateRequest(
        @NotNull Long storeId,
        @NotNull LocalDateTime pickupDate,

        Map<String, Object> orderData,

        @NotEmpty
        @Valid
        List<OrderItemRequest> items
) {}
