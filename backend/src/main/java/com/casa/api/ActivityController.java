package com.casa.api;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.casa.api.dto.activity.ActivityRequest;
import com.casa.api.dto.activity.ActivityResponse;
import com.casa.api.dto.activity.ActivityWeeklyStatsResponse;
import com.casa.domain.activities.ActivityType;
import com.casa.security.AppUserPrincipal;
import com.casa.service.ActivityService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/households/{householdId}/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public List<ActivityResponse> list(
        Authentication authentication,
        @PathVariable UUID householdId,
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) UUID petId,
        @RequestParam(required = false) ActivityType type
    ) {
        return activityService.list(currentUserId(authentication), householdId, userId, petId, type);
    }

    @PostMapping
    public ActivityResponse create(
        Authentication authentication,
        @PathVariable UUID householdId,
        @Valid @RequestBody ActivityRequest request
    ) {
        return activityService.create(currentUserId(authentication), householdId, request);
    }

    @GetMapping("/stats/weekly")
    public ActivityWeeklyStatsResponse weeklyStats(
        Authentication authentication,
        @PathVariable UUID householdId,
        @RequestParam(required = false) UUID userId,
        @RequestParam(required = false) UUID petId,
        @RequestParam(required = false) ActivityType type,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart
    ) {
        return activityService.weeklyStats(currentUserId(authentication), householdId, userId, petId, type, weekStart);
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
