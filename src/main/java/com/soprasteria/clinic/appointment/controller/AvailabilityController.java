package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.service.AvailabilityService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/availabilities")
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
        return availabilityService.addAvailability(availabilityDTO, id, authentication);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<?> getAllAvailabilities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return availabilityService.getAllAvailabilities(page, size);
    }

    @GetMapping("/doctor/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<?> getDoctorAvailabilities(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return availabilityService.getDoctorAvailabilities(id, page, size);
    }

    @PutMapping("/doctor/availability/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> updateAvailability(
            @RequestBody AvailabilityDTO availabilityDTO,
            @PathVariable Long availabilityId,
            Authentication authentication) {
        return availabilityService.updateAvailability(availabilityDTO, availabilityId, authentication);
    }

    @DeleteMapping("/doctor/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> deleteAvailability(
            @PathVariable Long id,
            Authentication authentication) {
        return availabilityService.deleteAvailability(id, doctorRepository.findById(id).get().getUsername(), authentication);
    }
}
