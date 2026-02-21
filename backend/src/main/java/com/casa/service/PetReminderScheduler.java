package com.casa.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PetReminderScheduler {

    private final PetService petService;

    public PetReminderScheduler(PetService petService) {
        this.petService = petService;
    }

    @Scheduled(cron = "0 15 */6 * * *")
    public void runPetReminders() {
        petService.scanAndNotifyReminders();
    }
}
