package com.soprasteria.clinic.appointment.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.service.DoctorService;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @Transactional
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
    @Transactional
    public ResponseEntity<?> updateDoctor(DoctorDTO updatedDoctorDTO, Long doctorId, String loginUsername) {
        try {
            Doctor existingDoctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> {
                        logger.error("Doctor not found with ID: {}", doctorId);
                        return new DoctorNotFoundException("Doctor not found with ID: " + doctorId);
                    });

            if (!existingDoctor.getUsername().equals(loginUsername)) {
                logger.warn("Unauthorized update attempt by user: {}", loginUsername);
                return new ResponseEntity<>(new UnauthorizedAccessException("Unauthorized update attempt"), HttpStatus.UNAUTHORIZED);
            }

            BeanUtils.copyProperties(updatedDoctorDTO, existingDoctor, NullPropertyUtils.getNullPropertyNames(updatedDoctorDTO));
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
    @Transactional
    public ResponseEntity<?> deleteDoctorById(Long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                logger.error("Doctor not found for deletion, ID: {}", id);
                throw new ClinicExceptionHandler.DoctorNotFoundException("Doctor with ID " + id + " not found");
            }

            doctorRepository.deleteById(id);
            logger.info("Doctor deleted with ID: {}", id);

            return ResponseEntity.ok("Doctor with ID " + id + " deleted successfully.");
        } catch (ClinicExceptionHandler.DoctorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting doctor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Deletion failed");
        }
    }
}
