package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import org.springframework.http.ResponseEntity;

public interface AvailabilityService {

    ResponseEntity<?> addAvailability(AvailabilityDTO availabilityDTO, Long doctorId, String loggedInUsername);

    ResponseEntity<?> getAllAvailabilities(int page, int size);

    ResponseEntity<?> getDoctorAvailabilities(Long doctorId, int page, int size);

    ResponseEntity<?> updateAvailability(AvailabilityDTO availabilityDTO, Long availabilityId, String loggedInUsername);

    ResponseEntity<?> deleteAvailability(Long id, String username);
}
