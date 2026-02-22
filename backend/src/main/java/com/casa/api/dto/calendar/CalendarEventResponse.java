package com.casa.api.dto.calendar;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.casa.domain.calendar.CalendarEventType;
import com.casa.domain.calendar.CalendarRecurrenceFrequency;

public record CalendarEventResponse(
    UUID id,
    String title,
    String description,
    OffsetDateTime startAt,
    OffsetDateTime endAt,
    OffsetDateTime occurrenceStartAt,
    OffsetDateTime occurrenceEndAt,
    CalendarEventType type,
    String colorHex,
    boolean allDay,
    CalendarRecurrenceFrequency recurrenceFrequency,
    int recurrenceInterval,
    Integer recurrenceCount,
    OffsetDateTime recurrenceUntil,
    Integer reminderMinutesBefore,
    UUID createdByUserId,
    UUID assignedToUserId
) {
}
