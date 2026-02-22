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

@Service
public class CalendarService {

    private final CalendarEventRepository calendarEventRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final HouseholdService householdService;

    public CalendarService(
        CalendarEventRepository calendarEventRepository,
        HouseholdRepository householdRepository,
        HouseholdMemberRepository householdMemberRepository,
        HouseholdService householdService
    ) {
        this.calendarEventRepository = calendarEventRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.householdService = householdService;
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

        calendarEventRepository.delete(event);
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

    private String cleanNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
