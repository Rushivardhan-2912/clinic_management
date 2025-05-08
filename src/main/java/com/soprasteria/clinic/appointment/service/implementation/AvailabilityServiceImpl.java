package com.soprasteria.clinic.appointment.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.entity.Availability;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.AvailabilityNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.DoctorNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.UnauthorizedAccessException;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.service.AvailabilityService;
import com.soprasteria.clinic.appointment.util.NullPropertyUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Logger logger = LogManager.getLogger(AvailabilityServiceImpl.class);

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private GlobalMapper globalMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    public ResponseEntity<?> addAvailability(AvailabilityDTO availabilityDTO, Long doctorId, Authentication authentication) {
        try {
            String loggedInUsername = authentication.getName();
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

            if (!doctor.getUsername().equals(loggedInUsername)) {
                throw new UnauthorizedAccessException("You are not authorized to add availability for this doctor.");
            }

            if (availabilityDTO.getAvailability_startTime() != null && availabilityDTO.getAvailability_endTime() != null &&
                    availabilityDTO.getAvailability_startTime().isAfter(availabilityDTO.getAvailability_endTime())) {
                return ResponseEntity.badRequest().body("Start time cannot be after end time.");
            }

            Availability availability = globalMapper.toAvailabilityEntity(availabilityDTO, doctor);
            availability.setAvailability_status("Available");

            Availability saved = availabilityRepository.save(availability);
            return ResponseEntity.status(HttpStatus.CREATED).body(globalMapper.toAvailabilityDTO(saved));
        } catch (Exception e) {
            logger.error("Error adding availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllAvailabilities(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Availability> availabilityPage = availabilityRepository.findAll(pageable);
            List<AvailabilityDTO> dtos = availabilityPage.map(globalMapper::toAvailabilityDTO).getContent();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error retrieving availabilities: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getDoctorAvailabilities(Long doctorId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Availability> availabilityPage = availabilityRepository.findBydoctorId(doctorId, pageable);
            List<AvailabilityDTO> dtos = availabilityPage.map(globalMapper::toAvailabilityDTO).getContent();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error retrieving doctor availabilities: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> updateAvailability(AvailabilityDTO availabilityDTO, Long availabilityId, Authentication authentication) {
        try {
            String loggedInUsername = authentication.getName();
            Availability availability = availabilityRepository.findById(availabilityId)
                    .orElseThrow(() -> new AvailabilityNotFoundException("Availability not found with ID: " + availabilityId));

            Doctor doctor = availability.getDoctor();
            if (!doctor.getUsername().equals(loggedInUsername)) {
                throw new UnauthorizedAccessException("You are not authorized to update this availability.");
            }

            if (availabilityDTO.getAvailability_startTime() != null && availabilityDTO.getAvailability_endTime() != null &&
                    availabilityDTO.getAvailability_startTime().isAfter(availabilityDTO.getAvailability_endTime())) {
                return ResponseEntity.badRequest().body("Start time cannot be after end time.");
            }

            BeanUtils.copyProperties(availabilityDTO, availability, NullPropertyUtils.getNullPropertyNames(availabilityDTO));
            availability.setDoctor(doctor);
            availability.setAvailability_id(availabilityId);
            availability.setAvailability_status("Available");

            Availability updated = availabilityRepository.save(availability);
            return ResponseEntity.ok(globalMapper.toAvailabilityDTO(updated));
        } catch (Exception e) {
            logger.error("Error updating availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> deleteAvailability(Long id, String username, Authentication authentication) {
        try {
            validateLoggedInUser(username, authentication);

            Availability availability = availabilityRepository.findById(id)
                    .orElseThrow(() -> new AvailabilityNotFoundException("Availability not found with ID: " + id));

            Doctor doctor = doctorRepository.findByUsername(username)
                    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with username: " + username));

            if (!availability.getDoctor().getDoctor_id().equals(doctor.getDoctor_id())) {
                throw new UnauthorizedAccessException("You are not authorized to delete this availability");
            }

            availabilityRepository.deleteById(id);
            return ResponseEntity.ok("Availability deleted successfully with ID: " + id);
        } catch (Exception e) {
            logger.error("Error deleting availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private void validateLoggedInUser(String username, Authentication authentication) {
        if (authentication == null || !authentication.getName().equals(username)) {
            throw new UnauthorizedAccessException("You are not authorized to perform this operation.");
        }
    }
}
