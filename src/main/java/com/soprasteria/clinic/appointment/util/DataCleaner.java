//package com.soprasteria.clinic.appointment.util;
//
//import com.soprasteria.clinic.appointment.repo.AppointmentRepository;
//import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
//import jakarta.transaction.Transactional;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDate;
//
//@Component
//public class DataCleaner implements CommandLineRunner {
//
//    private static final Logger logger = LogManager.getLogger(DataCleaner.class);
//
//    private final AvailabilityRepository availabilityRepository;
//    private final AppointmentRepository appointmentRepository;
//
//    public DataCleaner(AvailabilityRepository availabilityRepository, AppointmentRepository appointmentRepository) {
//        this.availabilityRepository = availabilityRepository;
//        this.appointmentRepository = appointmentRepository;
//    }
//
//    @Override
//    @Transactional
//    public void run(String... args) {
//
//        LocalDate today = LocalDate.now();
//        availabilityRepository.deletePastData(today);
//        appointmentRepository.deletePastData(today);
//        logger.info("Old data deleted on startup.");
//    }
//}
