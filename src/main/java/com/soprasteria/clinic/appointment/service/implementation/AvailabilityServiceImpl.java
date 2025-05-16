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
import com.soprasteria.clinic.appointment.util.Status;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.soprasteria.clinic.appointment.util.GenericMessages.*;

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
    @Transactional
    public ResponseEntity<?> addAvailability(AvailabilityDTO availabilityDTO, Long doctorId, String loggedInUsername) {
        try {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND,doctorId)));

            if (!doctor.getUsername().equals(loggedInUsername)) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            LocalTime startTime = availabilityDTO.getAvailabilityStartTime();
            LocalTime endTime = availabilityDTO.getAvailabilityEndTime();
            LocalDate date = availabilityDTO.getAvailabilityDate();

            if (startTime != null && endTime != null && !endTime.isAfter(startTime)) {
                return ResponseEntity.badRequest().body("Start time must be before end time.");
            }

            // Check for existing availability to prevent duplicates
            boolean exists = availabilityRepository.existsByDoctorAndDateAndTime(
                    doctorId, date, startTime, endTime
            );

            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("This availability slot already exists.");
            }

            Availability availability = globalMapper.toAvailabilityEntity(availabilityDTO, doctor);
            availability.setAvailabilityStatus(Status.AVAILABLE);

            Availability saved = availabilityRepository.save(availability);

            return ResponseEntity.status(HttpStatus.CREATED).body(globalMapper.toAvailabilityDTO(saved));

        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (DoctorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred while adding availability.");
        }
    }


    @Override
    public ResponseEntity<?> getAllAvailabilities() {
        try {
            List<Availability> availabilities = availabilityRepository.findAll();
            List<AvailabilityDTO> dtos = availabilities.stream()
                    .map(globalMapper::toAvailabilityDTO)
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error retrieving availabilities: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getDoctorAvailabilities(Long doctorId) {
        try {
            List<Availability> availabilities = availabilityRepository.findByDoctorId(doctorId);
            List<AvailabilityDTO> dtos = availabilities.stream()
                    .map(globalMapper::toAvailabilityDTO)
                    .toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error retrieving doctor availabilities: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> updateAvailability(AvailabilityDTO availabilityDTO, Long availabilityId, String loggedInUsername) {
        try {
            Availability availability = availabilityRepository.findById(availabilityId)
                    .orElseThrow(() -> new AvailabilityNotFoundException(String.format(AVAILABILITY_NOT_FOUND,availabilityId)));

            Doctor doctor = availability.getDoctor();
            if (!doctor.getUsername().equals(loggedInUsername)) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            if (availabilityDTO.getAvailabilityStartTime() != null && availabilityDTO.getAvailabilityEndTime() != null &&
                    availabilityDTO.getAvailabilityStartTime().isAfter(availabilityDTO.getAvailabilityEndTime())) {
                return ResponseEntity.badRequest().body("Start time cannot be after end time.");
            }

            BeanUtils.copyProperties(availabilityDTO, availability, NullPropertyUtils.getNullPropertyNames(availabilityDTO));
            availability.setDoctor(doctor);
            availability.setId(availabilityId);
            availability.setAvailabilityStatus( Status.AVAILABLE);

            Availability updated = availabilityRepository.save(availability);
            return ResponseEntity.ok(globalMapper.toAvailabilityDTO(updated));
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (AvailabilityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteAvailability(Long id, String username, Authentication authentication) {
        try {
            validateLoggedInUser(username, authentication);

            Availability availability = availabilityRepository.findById(id)
                    .orElseThrow(() -> new AvailabilityNotFoundException(String.format(AVAILABILITY_NOT_FOUND,id)));

            Doctor doctor = doctorRepository.findByUsername(username)
                    .orElseThrow(() -> new DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND,username)));

            if (!availability.getDoctor().getId().equals(doctor.getId())) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            availabilityRepository.deleteById(id);
            return ResponseEntity.ok("Availability deleted successfully with ID: " + id);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (AvailabilityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (DoctorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private void validateLoggedInUser(String username, Authentication authentication) {
        if (authentication == null || !authentication.getName().equals(username)) {
            throw new UnauthorizedAccessException(UNAUTHORIZED);
        }
    }
}
