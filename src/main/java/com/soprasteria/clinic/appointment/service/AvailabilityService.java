package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AvailabilityService {

    ResponseEntity<?> addAvailability(AvailabilityDTO availabilityDTO, Long doctorId, Authentication authentication);

    ResponseEntity<?> getAllAvailabilities(int page, int size);

    ResponseEntity<?> getDoctorAvailabilities(Long doctorId, int page, int size);

    ResponseEntity<?> updateAvailability(AvailabilityDTO availabilityDTO, Long availabilityId, Authentication authentication);

    ResponseEntity<?> deleteAvailability(Long id, String username, Authentication authentication);
}
