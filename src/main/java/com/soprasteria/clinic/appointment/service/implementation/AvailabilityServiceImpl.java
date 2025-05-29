package com.soprasteria.clinic.appointment.service.implementation;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.entity.Availability;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.StatusEnum;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.AvailabilityNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.DoctorNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.UnauthorizedAccessException;
import com.soprasteria.clinic.appointment.service.AvailabilityService;
import com.soprasteria.clinic.appointment.util.NullPropertyUtils;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.soprasteria.clinic.appointment.util.GenericMessages.*;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Logger logger = LogManager.getLogger(AvailabilityServiceImpl.class);

    private static final String ROLE ="ROLE_ADMIN";
    private static final String ERROR_DELETING="Error deleting availability: %s";
    private static final String ERROR_UPDATING="Error updating availability: {}";


    private final AvailabilityRepository availabilityRepository;
    private final GlobalMapper globalMapper;
    private final DoctorRepository doctorRepository;

    public AvailabilityServiceImpl(AvailabilityRepository availabilityRepository,
                                 GlobalMapper globalMapper,
                                 DoctorRepository doctorRepository) {
        this.availabilityRepository = availabilityRepository;
        this.globalMapper = globalMapper;
        this.doctorRepository = doctorRepository;
    }

    @Override
    @Transactional
    public ResponseEntity<?> addAvailability(AvailabilityDTO availabilityDTO, Long doctorId, String loggedInUsername) {
        try {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND, doctorId)));

            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(ROLE));

            if (!(isAdmin || doctor.getUsername().equals(loggedInUsername))) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            LocalTime startTime = availabilityDTO.getStartTime();
            LocalTime endTime = availabilityDTO.getEndTime();
            LocalDate date = availabilityDTO.getDate();

            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

            if (startDateTime.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body("Start time must be in the future.");
            }

            if (!endDateTime.isAfter(startDateTime)) {
                return ResponseEntity.badRequest().body("End time must be after start time.");
            }

            // Check for overlapping time slot
            if (hasOverlappingAvailability(doctorId, date, startTime, endTime)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("This availability slot overlaps with an existing one.");
            }

            if (availabilityDTO.getStatus() == null) {
                availabilityDTO.setStatus(StatusEnum.AVAILABLE);
            }
            logger.info("Added Availability with for doctor : {}",doctor.getUsername());
            Availability availability = globalMapper.toAvailabilityEntity(availabilityDTO, doctor);
            Availability saved = availabilityRepository.save(availability);

            return ResponseEntity.status(HttpStatus.CREATED).body(globalMapper.toAvailabilityDTO(saved));
        } catch (UnauthorizedAccessException e) {
            logger.error("Error adding availability : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (DoctorNotFoundException e) {
            logger.error("Error adding availability : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding availability: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred while adding availability.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateAvailability(AvailabilityDTO availabilityDTO, Long availabilityId, String loggedInUsername) {
        try {
            Availability availability = availabilityRepository.findById(availabilityId)
                    .orElseThrow(() -> new AvailabilityNotFoundException(String.format(AVAILABILITY_NOT_FOUND, availabilityId)));

            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(ROLE));

            Doctor doctor = availability.getDoctor();

            if (!(isAdmin || doctor.getUsername().equals(loggedInUsername))) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            LocalTime startTime = availabilityDTO.getStartTime();
            LocalTime endTime = availabilityDTO.getEndTime();
            LocalDate date = availabilityDTO.getDate();

            if (startTime != null && endTime != null && date != null) {
                LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
                LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

                if (startDateTime.isBefore(LocalDateTime.now())) {
                    return ResponseEntity.badRequest().body("Start time must be in the future.");
                }

                if (!endDateTime.isAfter(startDateTime)) {
                    return ResponseEntity.badRequest().body("End time must be after start time.");
                }

                if (hasOverlappingAvailabilityForUpdate(doctor.getId(), availabilityId, date, startTime, endTime)) {
                    return ResponseEntity.badRequest().body("Updated availability slot overlaps with another existing slot.");
                }
            }

            BeanUtils.copyProperties(availabilityDTO, availability, NullPropertyUtils.getNullPropertyNames(availabilityDTO));
            logger.info("Attempting to update the availability: {}",availabilityId);
            availability.setDoctor(doctor);
            availability.setId(availabilityId);
            availability.setStatus(StatusEnum.AVAILABLE);

            Availability updated = availabilityRepository.save(availability);
            logger.info("updated availability successfully");
            return ResponseEntity.ok(globalMapper.toAvailabilityDTO(updated));
        } catch (UnauthorizedAccessException e) {
            logger.error(String.format(ERROR_UPDATING), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (AvailabilityNotFoundException e) {
            logger.error(String.format(ERROR_UPDATING), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error adding availability: {}", e.getMessage(), e); // <-- include stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred while adding availability.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deleteAvailability(Long id, String username) {
        try {
            Availability availability = availabilityRepository.findById(id)
                    .orElseThrow(() -> new AvailabilityNotFoundException(String.format(AVAILABILITY_NOT_FOUND, id)));

            Doctor doctor = doctorRepository.findByUsername(username)
                    .orElseThrow(() -> new DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND, username)));

            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(ROLE));

            if (!(isAdmin || availability.getDoctor().getId().equals(doctor.getId()))) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            logger.info("Trying to delete availability with id: {}",id);
            availabilityRepository.deleteById(id);
            return ResponseEntity.ok("Availability deleted successfully with ID: " + id);
        } catch (UnauthorizedAccessException e) {
            logger.error(String.format(ERROR_DELETING), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (AvailabilityNotFoundException | DoctorNotFoundException e) {
            logger.error(String.format(ERROR_DELETING), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error(String.format(ERROR_DELETING), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> getAllAvailabilities(int page, int size) {
        try {
            logger.info("Retrieving all availabilities - Page: {}, Size: {}", page, size);

            Pageable pageable = PageRequest.of(page, size);
            Page<Availability> availabilityPage = availabilityRepository.findAll(pageable);

            List<AvailabilityDTO> dtos = availabilityPage.getContent().stream()
                    .map(globalMapper::toAvailabilityDTO)
                    .toList();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("data", dtos);
            response.put("pageNo", availabilityPage.getNumber());
            response.put("pageSize", availabilityPage.getSize());
            response.put("totalResults", availabilityPage.getTotalElements());
            response.put("totalPages", availabilityPage.getTotalPages());
            response.put("last", availabilityPage.isLast());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch availabilities");
        }
    }

    @Override
    public ResponseEntity<?> getDoctorAvailabilities(Long doctorId, int page, int size) {
        try {
            logger.info("Retrieving availabilities for doctor ID: {}, Page: {}, Size: {}", doctorId, page, size);

            Pageable pageable = PageRequest.of(page, size);
            Page<Availability> availabilityPage = availabilityRepository.findByDoctorId(doctorId, pageable);

            List<AvailabilityDTO> dtos = availabilityPage.getContent().stream()
                    .map(globalMapper::toAvailabilityDTO)
                    .toList();

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("data", dtos);
            response.put("pageNo", availabilityPage.getNumber());
            response.put("pageSize", availabilityPage.getSize());
            response.put("totalResults", availabilityPage.getTotalElements());
            response.put("totalPages", availabilityPage.getTotalPages());
            response.put("last", availabilityPage.isLast());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving doctor availabilities", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch doctor availabilities");
        }
    }

    // 🔁 Private method to check overlapping slots on ADD
    private boolean hasOverlappingAvailability(Long doctorId, LocalDate date, LocalTime start, LocalTime end) {
        return !availabilityRepository.findOverlappingAvailabilities(doctorId, date, start, end).isEmpty();
    }

    // 🔁 Private method to check overlapping slots on UPDATE
    private boolean hasOverlappingAvailabilityForUpdate(Long doctorId, Long availabilityId, LocalDate date, LocalTime start, LocalTime end) {
        return !availabilityRepository.findOverlappingAvailabilitiesForUpdate(doctorId, availabilityId, date, start, end).isEmpty();
    }
}
