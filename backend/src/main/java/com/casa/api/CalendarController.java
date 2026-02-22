package com.casa.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.casa.api.dto.calendar.CalendarEventRequest;
import com.casa.api.dto.calendar.CalendarEventResponse;
import com.casa.api.dto.integrations.GoogleCalendarStatusResponse;
import com.casa.domain.calendar.CalendarEventType;
import com.casa.security.AppUserPrincipal;
import com.casa.service.CalendarService;
import com.casa.service.GoogleCalendarService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/households/{householdId}/calendar/events")
public class CalendarController {

    private final CalendarService calendarService;
    private final GoogleCalendarService googleCalendarService;

    public CalendarController(CalendarService calendarService, GoogleCalendarService googleCalendarService) {
        this.calendarService = calendarService;
        this.googleCalendarService = googleCalendarService;
    }

    @GetMapping
    public List<CalendarEventResponse> list(
        Authentication authentication,
        @PathVariable UUID householdId,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
        @RequestParam(required = false) UUID createdByUserId,
        @RequestParam(required = false) CalendarEventType type
    ) {
        return calendarService.listEvents(currentUserId(authentication), householdId, from, to, createdByUserId, type);
    }

    @PostMapping
    public CalendarEventResponse create(
        Authentication authentication,
        @PathVariable UUID householdId,
        @Valid @RequestBody CalendarEventRequest request
    ) {
        return calendarService.createEvent(currentUserId(authentication), householdId, request);
    }

    @PutMapping("/{eventId}")
    public CalendarEventResponse update(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID eventId,
        @Valid @RequestBody CalendarEventRequest request
    ) {
        return calendarService.updateEvent(currentUserId(authentication), householdId, eventId, request);
    }

    @DeleteMapping("/{eventId}")
    public void delete(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID eventId
    ) {
        calendarService.deleteEvent(currentUserId(authentication), householdId, eventId);
    }

    @GetMapping("/google/status")
    public GoogleCalendarStatusResponse googleStatus(Authentication authentication, @PathVariable UUID householdId) {
        UUID userId = currentUserId(authentication);
        boolean linked = googleCalendarService.findLink(userId).isPresent();
        String calendarId = linked ? googleCalendarService.ensureCalendar(userId) : null;
        return new GoogleCalendarStatusResponse(linked, calendarId, "casaChiquitos");
    }

    @PostMapping("/google/sync")
    public void googleSync(Authentication authentication, @PathVariable UUID householdId) {
        calendarService.syncFromGoogle(currentUserId(authentication), householdId);
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
