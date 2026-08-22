package org.makery.dto;

public record NotificationSettingsUpdateRequest(
        Boolean orderPushEnabled,
        Boolean chatPushEnabled,
        Boolean marketingPushEnabled
) {
}
