package com.clinic.system.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @Async("taskExecutor")
    public void sendPatientRegistrationNotification(String patientName) {
        log.info("Sending notification for patient: {}", patientName);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Notification sent for patient: {}", patientName);
    }
}