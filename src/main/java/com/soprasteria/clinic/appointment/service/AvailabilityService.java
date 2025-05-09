package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AvailabilityService {

    ResponseEntity<?> addAvailability(AvailabilityDTO availabilityDTO, Long doctorId, String loggedInUsername);

    ResponseEntity<?> getAllAvailabilities();

    ResponseEntity<?> getDoctorAvailabilities(Long doctorId);

    ResponseEntity<?> updateAvailability(AvailabilityDTO availabilityDTO, Long availabilityId, String loggedInUsername);

    ResponseEntity<?> deleteAvailability(Long id, String username, Authentication authentication);
}
