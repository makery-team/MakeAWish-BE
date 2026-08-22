package org.makery.dto;

public record OrderStatusUpdateRequest(
        String status,
        String reason,
        String rejectReason
) {}
