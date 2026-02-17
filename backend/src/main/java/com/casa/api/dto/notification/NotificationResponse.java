package com.casa.api.dto.notification;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.casa.domain.notifications.NotificationType;

public record NotificationResponse(
    UUID id,
    NotificationType type,
    String title,
    String body,
    OffsetDateTime scheduledFor,
    OffsetDateTime readAt
) {
}
