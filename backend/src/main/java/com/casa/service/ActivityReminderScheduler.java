package com.casa.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ActivityReminderScheduler {

    private final ActivityService activityService;

    public ActivityReminderScheduler(ActivityService activityService) {
        this.activityService = activityService;
    }

    @Scheduled(cron = "0 0 20 * * *")
    public void runLowActivityReminder() {
        activityService.scanLowActivityAndNotify();
    }
}
