package com.soprasteria.clinic.appointment.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.DoctorNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.UnauthorizedAccessException;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.service.DoctorService;
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

@Service
public class DoctorServiceImpl implements DoctorService {

    private static final Logger logger = LogManager.getLogger(DoctorServiceImpl.class);

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GlobalMapper globalMapper;

    @Override
    public ResponseEntity<?> findByUsername(String username) {
        try {
            logger.info("Finding doctor with username: {}", username);
            return doctorRepository.findByUsername(username)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found"));
        } catch (Exception e) {
            logger.error("Error finding doctor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve doctor");
        }
    }

    @Override
    public ResponseEntity<?> registerDoctor(Doctor doctor) {
        try {
            logger.info("Registering new doctor with username: {}", doctor.getUsername());
            Doctor savedDoctor = doctorRepository.save(doctor);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDoctor);
        } catch (Exception e) {
            logger.error("Error during doctor registration", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed");
        }
    }

    @Override
    public ResponseEntity<?> getAllDoctors(int page, int size, Authentication authentication) {
        try {
            String loginUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
            boolean isPatient = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_PATIENT"));

            if (!(isAdmin || isPatient)) {
                logger.warn("Access denied for user: {}", loginUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You are not authorized to view the doctor list.");
            }

            logger.info("Fetching all doctors - user: {}, page: {}, size: {}", loginUsername, page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<Doctor> doctorsPage = doctorRepository.findAll(pageable);
            return ResponseEntity.ok(doctorsPage.map(globalMapper::toDoctorDTO).getContent());

        } catch (Exception e) {
            logger.error("Error retrieving doctor list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve doctors");
        }
    }

    @Override
    public ResponseEntity<?> updateDoctor(DoctorDTO updatedDoctorDTO, Long doctorId, Authentication authentication) {
        try {
            String loginUsername = authentication.getName();

            Doctor existingDoctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> {
                        logger.error("Doctor not found with ID: {}", doctorId);
                        return new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
                    });

            if (!existingDoctor.getUsername().equals(loginUsername)) {
                logger.warn("Unauthorized update attempt by user: {}", loginUsername);
                return new ResponseEntity<>(new UnauthorizedAccessException("Unauthorized update attempt"), HttpStatus.UNAUTHORIZED);
            }

            BeanUtils.copyProperties(updatedDoctorDTO, existingDoctor,
                    NullPropertyUtils.getNullPropertyNames(updatedDoctorDTO));
            Doctor savedDoctor = doctorRepository.save(existingDoctor);
            return ResponseEntity.ok(globalMapper.toDoctorDTO(savedDoctor));

        } catch (DoctorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating doctor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Update failed");
        }
    }

    @Override
    public ResponseEntity<?> deleteDoctorById(Long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                logger.error("Doctor not found for deletion, ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Doctor not found with ID: " + id);
            }

            doctorRepository.deleteById(id);
            logger.info("Doctor deleted with ID: {}", id);
            return ResponseEntity.ok("Doctor with ID " + id + " deleted successfully.");
        } catch (Exception e) {
            logger.error("Error deleting doctor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Deletion failed");
        }
    }
}
