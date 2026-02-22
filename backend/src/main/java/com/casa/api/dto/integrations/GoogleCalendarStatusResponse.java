package com.casa.api.dto.integrations;

public record GoogleCalendarStatusResponse(
    boolean linked,
    String calendarId,
    String calendarName
) {
}
