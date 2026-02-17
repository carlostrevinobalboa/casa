package com.casa.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PantryAlertScheduler {

    private final PantryAlertService pantryAlertService;

    public PantryAlertScheduler(PantryAlertService pantryAlertService) {
        this.pantryAlertService = pantryAlertService;
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void runPantryAlerts() {
        pantryAlertService.scanAllItemsAndNotify();
    }
}
