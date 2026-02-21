package com.casa.api.dto.notification;

import java.util.UUID;

public record NotificationRealtimeEvent(
    UUID householdId,
    long unreadCount,
    NotificationResponse notification
) {
}
