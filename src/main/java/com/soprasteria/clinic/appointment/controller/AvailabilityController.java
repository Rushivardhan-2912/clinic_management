package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.service.AvailabilityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/availabilities")
@SecurityRequirement( name = "bearerAuth")
public class AvailabilityController {

    private static final Logger logger = LogManager.getLogger(AvailabilityController.class);

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private DoctorRepository doctorRepository;

    @PostMapping("/doctor/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> addAvailability(
            @RequestBody AvailabilityDTO availabilityDTO,
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Attempting to add availability for doctor ID: {} by user: {}", id, authentication.getName());
        return availabilityService.addAvailability(availabilityDTO, id, authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<?> getAllAvailabilities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Retrieving all availabilities");
        return availabilityService.getAllAvailabilities();
    }

    @GetMapping("/doctor/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<?> getDoctorAvailabilities(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Retrieving availabilities for doctor ID: {} ", id );
        return availabilityService.getDoctorAvailabilities(id);
    }

    @PutMapping("/doctor/availability/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateAvailability(
            @RequestBody AvailabilityDTO availabilityDTO,
            @PathVariable Long availabilityId,
            Authentication authentication) {
        logger.info("Attempting to update availability ID: {} by user: {}", availabilityId, authentication.getName());
        return availabilityService.updateAvailability(availabilityDTO, availabilityId, authentication.getName());
    }

    @DeleteMapping("/doctor/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> deleteAvailability(
            @PathVariable Long id,
            Authentication authentication) {
        logger.info("Attempting to delete availability with ID: {} by user: {}", id, authentication.getName());
        return availabilityService.deleteAvailability(id, doctorRepository.findById(id).get().getUsername(), authentication);
    }
}
