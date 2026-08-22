package org.makery.dto;

import org.makery.domain.User;

public record NotificationSettingsResponse(
        boolean orderPushEnabled,
        boolean chatPushEnabled,
        boolean marketingPushEnabled
) {
    public static NotificationSettingsResponse from(User user) {
        return new NotificationSettingsResponse(
                user.isOrderPushEnabled(),
                user.isChatPushEnabled(),
                user.isMarketingPushEnabled()
        );
    }
}
