package com.soprasteria.clinic.appointment.config;

import com.soprasteria.clinic.appointment.repo.AppointmentRepository;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class TimeBasedStatusUpdater {

    private static final Logger logger = LogManager.getLogger(TimeBasedStatusUpdater.class);


    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    public TimeBasedStatusUpdater(AvailabilityRepository availabilityRepository, AppointmentRepository appointmentRepository) {
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
    }

    // Runs at 12:00 AM and 12:00 PM every day.

    @Scheduled(cron = "0 0 0,12 * * ?")
    @Transactional
    public void cleanOldData() {

        logger.info("Starting scheduled cleanup on {}", LocalDateTime.now());

        int expiredAvailabilities = availabilityRepository.markPastAsExpired(LocalDate.now(),LocalTime.now());
        int expiredAppointments = appointmentRepository.markPastAsExpired(LocalDate.now(), LocalTime.now());

        logger.info("Marked {} past availabilities as EXPIRED.", expiredAvailabilities);
        logger.info("Marked {} past appointments as COMPLETED.", expiredAppointments);
    }

}
