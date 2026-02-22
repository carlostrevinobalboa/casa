package com.casa.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.calendar.CalendarEventRequest;
import com.casa.api.dto.calendar.CalendarEventResponse;
import com.casa.domain.calendar.CalendarEvent;
import com.casa.domain.calendar.CalendarEventType;
import com.casa.domain.calendar.CalendarRecurrenceFrequency;
import com.casa.domain.household.Household;
import com.casa.repository.CalendarEventRepository;
import com.casa.repository.HouseholdMemberRepository;
import com.casa.repository.HouseholdRepository;
import com.casa.service.GoogleCalendarService.GoogleCalendarSyncResult;
import com.casa.service.GoogleCalendarService.CalendarImportHandler;

@Service
public class CalendarService {

    private final CalendarEventRepository calendarEventRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdService householdService;
    private final GoogleCalendarService googleCalendarService;

    public CalendarService(
        CalendarEventRepository calendarEventRepository,
        HouseholdRepository householdRepository,
        HouseholdMemberRepository householdMemberRepository,
        HouseholdService householdService,
        GoogleCalendarService googleCalendarService
    ) {
        this.calendarEventRepository = calendarEventRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.householdService = householdService;
        this.googleCalendarService = googleCalendarService;
    }

    @Transactional(readOnly = true)
    public List<CalendarEventResponse> listEvents(
        UUID userId,
        UUID householdId,
        OffsetDateTime from,
        OffsetDateTime to,
        UUID createdByUserId,
        CalendarEventType type
    ) {
        householdService.requireMembership(userId, householdId);

        if (createdByUserId != null
            && !householdMemberRepository.existsByHouseholdIdAndUserId(householdId, createdByUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario filtrado no pertenece al hogar");
        }

        OffsetDateTime fromValue = from == null ? OffsetDateTime.now(ZoneOffset.UTC).minusMonths(1) : from;
        OffsetDateTime toValue = to == null ? OffsetDateTime.now(ZoneOffset.UTC).plusMonths(1) : to;

        List<CalendarEvent> events = createdByUserId == null
            ? calendarEventRepository.findByHouseholdIdOrderByStartAtAsc(householdId)
            : calendarEventRepository.findByHouseholdIdAndCreatedByUserIdOrderByStartAtAsc(
                householdId,
                createdByUserId
            );

        List<CalendarEventResponse> responses = new ArrayList<>();
        for (CalendarEvent event : events) {
            if (type != null && event.getType() != type) {
                continue;
            }
            responses.addAll(expandEvent(event, fromValue, toValue));
        }

        return responses;
    }

    @Transactional
    public CalendarEventResponse createEvent(UUID userId, UUID householdId, CalendarEventRequest request) {
        householdService.requireMembership(userId, householdId);

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

        CalendarEvent event = new CalendarEvent();
        event.setId(UUID.randomUUID());
        event.setHousehold(household);
        event.setCreatedByUserId(userId);
        applyEvent(event, request);
        event.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        event.setUpdatedAt(event.getCreatedAt());

        calendarEventRepository.save(event);

        syncToGoogle(event);
        return toResponse(event, event.getStartAt(), event.getEndAt());
    }

    @Transactional
    public CalendarEventResponse updateEvent(UUID userId, UUID householdId, UUID eventId, CalendarEventRequest request) {
        householdService.requireMembership(userId, householdId);

        CalendarEvent event = calendarEventRepository.findById(eventId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        if (!event.getHousehold().getId().equals(householdId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }

        applyEvent(event, request);
        event.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        calendarEventRepository.save(event);

        syncToGoogle(event);

        return toResponse(event, event.getStartAt(), event.getEndAt());
    }

    @Transactional
    public void deleteEvent(UUID userId, UUID householdId, UUID eventId) {
        householdService.requireMembership(userId, householdId);

        CalendarEvent event = calendarEventRepository.findById(eventId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));

        if (!event.getHousehold().getId().equals(householdId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado");
        }

        syncDeleteToGoogle(event);
        calendarEventRepository.delete(event);
    }

    @Transactional
    public void syncFromGoogle(UUID userId, UUID householdId) {
        householdService.requireMembership(userId, householdId);

        googleCalendarService.refreshFromGoogle(userId, (CalendarImportHandler) (calendarId, googleEvent) -> {
            if (calendarId == null || calendarId.isBlank()) {
                return;
            }
            String googleEventId = getString(googleEvent.get("id"));
            if (googleEventId == null) {
                return;
            }

            CalendarEvent event = calendarEventRepository.findByGoogleEventIdAndGoogleCalendarId(
                googleEventId,
                calendarId
            )
                .orElseGet(() -> {
                    CalendarEvent created = new CalendarEvent();
                    created.setId(UUID.randomUUID());
                    created.setHousehold(householdRepository.getReferenceById(householdId));
                    created.setCreatedByUserId(userId);
                    created.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    return created;
                });

            event.setGoogleCalendarId(calendarId);
            applyGoogleEvent(event, googleEvent);
            event.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            calendarEventRepository.save(event);
        });
    }

    private void applyEvent(CalendarEvent event, CalendarEventRequest request) {
        OffsetDateTime startAt = request.startAt();
        OffsetDateTime endAt = request.endAt();
        if (endAt.isBefore(startAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha fin debe ser posterior al inicio");
        }

        event.setTitle(request.title().trim());
        event.setDescription(cleanNullable(request.description()));
        event.setStartAt(startAt);
        event.setEndAt(endAt);
        event.setType(request.type());
        event.setColorHex(cleanNullable(request.colorHex()));
        event.setAllDay(request.allDay());
        event.setAssignedToUserId(request.assignedToUserId());

        CalendarRecurrenceFrequency frequency = request.recurrenceFrequency() == null
            ? CalendarRecurrenceFrequency.NONE
            : request.recurrenceFrequency();
        int interval = request.recurrenceInterval() == null ? 1 : Math.max(1, request.recurrenceInterval());

        event.setRecurrenceFrequency(frequency);
        event.setRecurrenceInterval(interval);
        event.setRecurrenceCount(request.recurrenceCount());
        event.setRecurrenceUntil(request.recurrenceUntil());
        event.setReminderMinutesBefore(request.reminderMinutesBefore());
    }

    private void applyGoogleEvent(CalendarEvent event, java.util.Map<?, ?> googleEvent) {
        event.setGoogleEventId(getString(googleEvent.get("id")));
        event.setTitle(getString(googleEvent.get("summary")) == null ? "Evento" : getString(googleEvent.get("summary")));
        event.setDescription(getString(googleEvent.get("description")));
        event.setType(CalendarEventType.OTHER);
        event.setColorHex("#0f172a");
        boolean allDay = isAllDayEvent(googleEvent);
        event.setAllDay(allDay);
        event.setRecurrenceFrequency(CalendarRecurrenceFrequency.NONE);
        event.setRecurrenceInterval(1);
        event.setRecurrenceCount(null);
        event.setRecurrenceUntil(null);
        event.setReminderMinutesBefore(null);

        Object start = googleEvent.get("start");
        Object end = googleEvent.get("end");

        OffsetDateTime startAt = parseGoogleDateTime(start);
        OffsetDateTime endAt = parseGoogleDateTime(end);
        if (startAt != null) {
            event.setStartAt(startAt);
        }
        if (endAt != null) {
            event.setEndAt(endAt);
        }
    }

    private OffsetDateTime parseGoogleDateTime(Object container) {
        if (!(container instanceof java.util.Map<?, ?> map)) {
            return null;
        }
        Object dateTime = map.get("dateTime");
        if (dateTime instanceof String value) {
            return OffsetDateTime.parse(value);
        }
        Object date = map.get("date");
        if (date instanceof String value) {
            return OffsetDateTime.parse(value + "T00:00:00Z");
        }
        return null;
    }

    private boolean isAllDayEvent(java.util.Map<?, ?> googleEvent) {
        Object start = googleEvent.get("start");
        Object end = googleEvent.get("end");
        boolean startAllDay = hasDateOnly(start);
        boolean endAllDay = hasDateOnly(end);
        return startAllDay || endAllDay;
    }

    private boolean hasDateOnly(Object container) {
        if (!(container instanceof java.util.Map<?, ?> map)) {
            return false;
        }
        Object date = map.get("date");
        return date instanceof String && !((String) date).isBlank();
    }

    private String getString(Object value) {
        return value instanceof String str ? str : null;
    }

    private List<CalendarEventResponse> expandEvent(CalendarEvent event, OffsetDateTime from, OffsetDateTime to) {
        List<CalendarEventResponse> responses = new ArrayList<>();
        CalendarRecurrenceFrequency frequency = event.getRecurrenceFrequency();

        if (frequency == null || frequency == CalendarRecurrenceFrequency.NONE) {
            if (overlaps(event.getStartAt(), event.getEndAt(), from, to)) {
                responses.add(toResponse(event, event.getStartAt(), event.getEndAt()));
            }
            return responses;
        }

        OffsetDateTime occurrenceStart = event.getStartAt();
        OffsetDateTime occurrenceEnd = event.getEndAt();
        int interval = Math.max(1, event.getRecurrenceInterval());
        int count = 0;

        while (!occurrenceStart.isAfter(to)) {
            if (overlaps(occurrenceStart, occurrenceEnd, from, to)) {
                responses.add(toResponse(event, occurrenceStart, occurrenceEnd));
            }

            count += 1;
            if (event.getRecurrenceCount() != null && count >= event.getRecurrenceCount()) {
                break;
            }

            occurrenceStart = nextOccurrence(occurrenceStart, frequency, interval);
            occurrenceEnd = nextOccurrence(occurrenceEnd, frequency, interval);

            if (event.getRecurrenceUntil() != null && occurrenceStart.isAfter(event.getRecurrenceUntil())) {
                break;
            }
        }

        return responses;
    }

    private OffsetDateTime nextOccurrence(
        OffsetDateTime value,
        CalendarRecurrenceFrequency frequency,
        int interval
    ) {
        return switch (frequency) {
            case DAILY -> value.plusDays(interval);
            case WEEKLY -> value.plusWeeks(interval);
            case MONTHLY -> value.plusMonths(interval);
            case NONE -> value;
        };
    }

    private boolean overlaps(OffsetDateTime start, OffsetDateTime end, OffsetDateTime from, OffsetDateTime to) {
        return !end.isBefore(from) && !start.isAfter(to);
    }

    private CalendarEventResponse toResponse(
        CalendarEvent event,
        OffsetDateTime occurrenceStartAt,
        OffsetDateTime occurrenceEndAt
    ) {
        return new CalendarEventResponse(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getStartAt(),
            event.getEndAt(),
            occurrenceStartAt,
            occurrenceEndAt,
            event.getType(),
            event.getColorHex(),
            event.isAllDay(),
            event.getRecurrenceFrequency(),
            event.getRecurrenceInterval(),
            event.getRecurrenceCount(),
            event.getRecurrenceUntil(),
            event.getReminderMinutesBefore(),
            event.getCreatedByUserId(),
            event.getAssignedToUserId()
        );
    }

    private void syncToGoogle(CalendarEvent event) {
        UUID syncUserId = event.getAssignedToUserId() == null ? event.getCreatedByUserId() : event.getAssignedToUserId();
        googleCalendarService.findLink(syncUserId).ifPresent(link -> {
            GoogleCalendarSyncResult syncResult = googleCalendarService.upsertEvent(syncUserId, event);
            if (syncResult == null || syncResult.eventId() == null) {
                return;
            }
            boolean shouldSave = false;
            if (!syncResult.eventId().equals(event.getGoogleEventId())) {
                event.setGoogleEventId(syncResult.eventId());
                shouldSave = true;
            }
            if (syncResult.calendarId() != null && !syncResult.calendarId().equals(event.getGoogleCalendarId())) {
                event.setGoogleCalendarId(syncResult.calendarId());
                shouldSave = true;
            }
            if (shouldSave) {
                calendarEventRepository.save(event);
            }
        });
    }

    private void syncDeleteToGoogle(CalendarEvent event) {
        UUID syncUserId = event.getAssignedToUserId() == null ? event.getCreatedByUserId() : event.getAssignedToUserId();
        googleCalendarService.findLink(syncUserId).ifPresent(link -> googleCalendarService.deleteEvent(syncUserId, event));
    }

    private String cleanNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
