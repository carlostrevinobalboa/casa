package com.casa.api;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.casa.api.dto.notification.NotificationResponse;
import com.casa.security.AppUserPrincipal;
import com.casa.service.NotificationService;

@RestController
@RequestMapping("/api/households/{householdId}/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list(Authentication authentication, @PathVariable UUID householdId) {
        return notificationService.list(currentUserId(authentication), householdId);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication, @PathVariable UUID householdId) {
        long count = notificationService.unreadCount(currentUserId(authentication), householdId);
        return Map.of("unreadCount", count);
    }

    @PostMapping("/{notificationId}/read")
    public void markAsRead(
        Authentication authentication,
        @PathVariable UUID householdId,
        @PathVariable UUID notificationId
    ) {
        notificationService.markAsRead(currentUserId(authentication), householdId, notificationId);
    }

    private UUID currentUserId(Authentication authentication) {
        AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
        return principal.userId();
    }
}
