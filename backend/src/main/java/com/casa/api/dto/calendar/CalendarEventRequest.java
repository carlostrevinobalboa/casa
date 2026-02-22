package com.casa.api.dto.calendar;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.casa.domain.calendar.CalendarEventType;
import com.casa.domain.calendar.CalendarRecurrenceFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CalendarEventRequest(
    @NotBlank @Size(max = 160) String title,
    @Size(max = 600) String description,
    @NotNull OffsetDateTime startAt,
    @NotNull OffsetDateTime endAt,
    @NotNull CalendarEventType type,
    @Size(max = 16) String colorHex,
    boolean allDay,
    CalendarRecurrenceFrequency recurrenceFrequency,
    @PositiveOrZero Integer recurrenceInterval,
    @PositiveOrZero Integer recurrenceCount,
    OffsetDateTime recurrenceUntil,
    @PositiveOrZero Integer reminderMinutesBefore,
    UUID assignedToUserId
) {
}
