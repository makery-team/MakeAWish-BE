package org.makery.dto;

public record DeviceTokenRequest(
        String token,
        String platform
) {
}
