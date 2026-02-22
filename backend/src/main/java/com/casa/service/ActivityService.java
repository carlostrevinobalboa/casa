package com.casa.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.casa.api.dto.activity.ActivityDailyStatResponse;
import com.casa.api.dto.activity.ActivityPointRequest;
import com.casa.api.dto.activity.ActivityPointResponse;
import com.casa.api.dto.activity.ActivityRequest;
import com.casa.api.dto.activity.ActivityResponse;
import com.casa.api.dto.activity.ActivityWeeklyStatsResponse;
import com.casa.domain.activities.ActivityPoint;
import com.casa.domain.activities.ActivitySession;
import com.casa.domain.activities.ActivityType;
import com.casa.domain.household.Household;
import com.casa.domain.notifications.NotificationType;
import com.casa.domain.pets.Pet;
import com.casa.repository.ActivityPointRepository;
import com.casa.repository.ActivitySessionRepository;
import com.casa.repository.HouseholdMemberRepository;
import com.casa.repository.HouseholdRepository;
import com.casa.repository.PetRepository;

@Service
public class ActivityService {

    private static final long LOW_ACTIVITY_MIN_SESSIONS_PER_WEEK = 2;

    private final ActivitySessionRepository activitySessionRepository;
    private final ActivityPointRepository activityPointRepository;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final PetRepository petRepository;
    private final HouseholdService householdService;
    private final NotificationService notificationService;

    public ActivityService(
        ActivitySessionRepository activitySessionRepository,
        ActivityPointRepository activityPointRepository,
        HouseholdRepository householdRepository,
        HouseholdMemberRepository householdMemberRepository,
        PetRepository petRepository,
        HouseholdService householdService,
        NotificationService notificationService
    ) {
        this.activitySessionRepository = activitySessionRepository;
        this.activityPointRepository = activityPointRepository;
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.petRepository = petRepository;
        this.householdService = householdService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> list(
        UUID userId,
        UUID householdId,
        UUID filterUserId,
        UUID petId,
        ActivityType type
    ) {
        householdService.requireMembership(userId, householdId);

        if (filterUserId != null && !householdMemberRepository.existsByHouseholdIdAndUserId(householdId, filterUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario filtrado no pertenece al hogar");
        }

        if (petId != null) {
            requirePet(householdId, petId);
        }

        List<ActivitySession> sessions = filterUserId == null
            ? activitySessionRepository.findTop200ByHouseholdIdOrderByStartedAtDesc(householdId)
            : activitySessionRepository.findTop200ByHouseholdIdAndPerformedByUserIdOrderByStartedAtDesc(householdId, filterUserId);

        return sessions.stream()
            .filter(session -> type == null || session.getType() == type)
            .filter(session -> petId == null || petId.equals(session.getPetId()))
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public ActivityResponse create(UUID userId, UUID householdId, ActivityRequest request) {
        householdService.requireMembership(userId, householdId);

        Household household = householdRepository.findById(householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hogar no encontrado"));

        UUID performedByUserId = request.performedByUserId() == null ? userId : request.performedByUserId();
        if (!householdMemberRepository.existsByHouseholdIdAndUserId(householdId, performedByUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario seleccionado no pertenece al hogar");
        }

        Pet pet = null;
        if (request.petId() != null) {
            pet = requirePet(householdId, request.petId());
        }
        if (request.type() == ActivityType.PET_WALK && pet == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una mascota para el paseo");
        }

        OffsetDateTime startedAt = request.startedAt();
        OffsetDateTime endedAt = request.endedAt();
        if (endedAt.isBefore(startedAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha fin debe ser posterior al inicio");
        }

        List<ActivityPointRequest> pointsRequest = request.points() == null ? List.of() : request.points();
        double distanceKm = request.distanceKm() == null ? estimateDistanceKm(pointsRequest) : request.distanceKm();
        distanceKm = Math.max(0.0, distanceKm);

        ActivitySession session = new ActivitySession();
        session.setHousehold(household);
        session.setType(request.type());
        session.setPerformedByUserId(performedByUserId);
        session.setPetId(request.petId());
        session.setStartedAt(startedAt);
        session.setEndedAt(endedAt);
        session.setDurationSeconds(Math.max(0, ChronoUnit.SECONDS.between(startedAt, endedAt)));
        session.setDistanceKm(roundDistanceKm(distanceKm));
        session.setTitle(cleanNullable(request.title()));
        session.setNotes(cleanNullable(request.notes()));
        session.setGpsTracked(Boolean.TRUE.equals(request.gpsTracked()) || pointsRequest.size() > 1);
        activitySessionRepository.save(session);

        savePoints(session, pointsRequest);

        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public ActivityWeeklyStatsResponse weeklyStats(
        UUID userId,
        UUID householdId,
        UUID filterUserId,
        UUID petId,
        ActivityType type,
        LocalDate weekStart
    ) {
        householdService.requireMembership(userId, householdId);

        if (filterUserId != null && !householdMemberRepository.existsByHouseholdIdAndUserId(householdId, filterUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario filtrado no pertenece al hogar");
        }

        if (petId != null) {
            requirePet(householdId, petId);
        }

        LocalDate normalizedWeekStart = normalizeWeekStart(weekStart == null ? LocalDate.now() : weekStart);
        LocalDate weekEnd = normalizedWeekStart.plusDays(6);
        OffsetDateTime from = normalizedWeekStart.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime to = weekEnd.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        List<ActivitySession> sessions = filterUserId == null
            ? activitySessionRepository.findByHouseholdIdAndStartedAtBetweenOrderByStartedAtDesc(householdId, from, to)
            : activitySessionRepository.findByHouseholdIdAndPerformedByUserIdAndStartedAtBetweenOrderByStartedAtDesc(
                householdId,
                filterUserId,
                from,
                to
            );

        List<ActivitySession> filtered = sessions.stream()
            .filter(session -> type == null || session.getType() == type)
            .filter(session -> petId == null || petId.equals(session.getPetId()))
            .toList();

        Map<LocalDate, DailyAccumulator> dailyAcc = initDailyAccumulator(normalizedWeekStart);
        double totalDistance = 0.0;
        long totalDurationSeconds = 0;

        for (ActivitySession session : filtered) {
            LocalDate day = session.getStartedAt().toLocalDate();
            DailyAccumulator acc = dailyAcc.get(day);
            if (acc == null) {
                continue;
            }
            acc.activitiesCount += 1;
            acc.distanceKm += session.getDistanceKm();
            acc.durationSeconds += session.getDurationSeconds();

            totalDistance += session.getDistanceKm();
            totalDurationSeconds += session.getDurationSeconds();
        }

        List<ActivityDailyStatResponse> daily = dailyAcc.entrySet()
            .stream()
            .map(entry -> new ActivityDailyStatResponse(
                entry.getKey(),
                entry.getValue().activitiesCount,
                roundDistanceKm(entry.getValue().distanceKm),
                Math.round(entry.getValue().durationSeconds / 60.0)
            ))
            .toList();

        return new ActivityWeeklyStatsResponse(
            normalizedWeekStart,
            weekEnd,
            filtered.size(),
            roundDistanceKm(totalDistance),
            Math.round(totalDurationSeconds / 60.0),
            daily
        );
    }

    @Transactional
    public void scanLowActivityAndNotify() {
        OffsetDateTime from = OffsetDateTime.now().minusDays(7);
        List<UUID> households = householdRepository.findAll().stream().map(Household::getId).toList();

        for (UUID householdId : households) {
            List<UUID> users = householdService.householdUserIds(householdId);
            for (UUID targetUserId : users) {
                long sessionsCount = activitySessionRepository.countByHouseholdIdAndPerformedByUserIdAndStartedAtGreaterThanEqual(
                    householdId,
                    targetUserId,
                    from
                );

                if (sessionsCount >= LOW_ACTIVITY_MIN_SESSIONS_PER_WEEK) {
                    continue;
                }

                String title = "Actividad fisica baja";
                String body = "Llevas " + sessionsCount + " actividades en 7 dias. Intenta moverte hoy.";

                notificationService.createIfMissing(
                    householdId,
                    targetUserId,
                    NotificationType.LOW_PHYSICAL_ACTIVITY,
                    title,
                    body,
                    "USER_ACTIVITY",
                    targetUserId
                );
            }
        }
    }

    private ActivityResponse toResponse(ActivitySession session) {
        List<ActivityPointResponse> points = activityPointRepository.findByActivityIdOrderBySequenceNumberAsc(session.getId())
            .stream()
            .map(point -> new ActivityPointResponse(
                point.getId(),
                point.getSequenceNumber(),
                point.getLatitude(),
                point.getLongitude(),
                point.getRecordedAt()
            ))
            .toList();

        return new ActivityResponse(
            session.getId(),
            session.getType(),
            session.getPerformedByUserId(),
            session.getPetId(),
            session.getStartedAt(),
            session.getEndedAt(),
            session.getDurationSeconds(),
            session.getDistanceKm(),
            session.getTitle(),
            session.getNotes(),
            session.isGpsTracked(),
            points
        );
    }

    private void savePoints(ActivitySession session, List<ActivityPointRequest> points) {
        if (points == null || points.isEmpty()) {
            return;
        }

        List<ActivityPoint> entities = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            ActivityPointRequest pointRequest = points.get(i);
            ActivityPoint point = new ActivityPoint();
            point.setActivity(session);
            point.setSequenceNumber(i + 1);
            point.setLatitude(pointRequest.latitude());
            point.setLongitude(pointRequest.longitude());
            point.setRecordedAt(pointRequest.recordedAt() == null ? session.getStartedAt() : pointRequest.recordedAt());
            entities.add(point);
        }
        activityPointRepository.saveAll(entities);
    }

    private Pet requirePet(UUID householdId, UUID petId) {
        return petRepository.findByIdAndHouseholdId(petId, householdId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mascota no encontrada en el hogar"));
    }

    private double estimateDistanceKm(List<ActivityPointRequest> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }

        double distanceMeters = 0.0;
        for (int i = 1; i < points.size(); i++) {
            ActivityPointRequest previous = points.get(i - 1);
            ActivityPointRequest current = points.get(i);
            distanceMeters += haversineMeters(previous.latitude(), previous.longitude(), current.latitude(), current.longitude());
        }
        return distanceMeters / 1000.0;
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private double roundDistanceKm(double value) {
        return Double.parseDouble(String.format(Locale.ROOT, "%.3f", value));
    }

    private String cleanNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDate normalizeWeekStart(LocalDate date) {
        LocalDate current = date;
        while (current.getDayOfWeek() != DayOfWeek.MONDAY) {
            current = current.minusDays(1);
        }
        return current;
    }

    private Map<LocalDate, DailyAccumulator> initDailyAccumulator(LocalDate weekStart) {
        Map<LocalDate, DailyAccumulator> map = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            map.put(weekStart.plusDays(i), new DailyAccumulator());
        }
        return map;
    }

    private static final class DailyAccumulator {
        private int activitiesCount;
        private double distanceKm;
        private long durationSeconds;
    }
}
