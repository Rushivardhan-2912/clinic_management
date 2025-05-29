package com.soprasteria.clinic.appointment.service.implementation;

import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.PatientNotFoundException;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AdminRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.PatientService;
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static com.soprasteria.clinic.appointment.util.GenericMessages.*;

@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LogManager.getLogger(PatientServiceImpl.class);

    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final GlobalMapper globalMapper;

    public PatientServiceImpl(PatientRepository patientRepository, AdminRepository adminRepository, DoctorRepository doctorRepository,
                              GlobalMapper globalMapper) {
        this.patientRepository = patientRepository;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.globalMapper = globalMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<?> registerPatient(Patient patient) {
        try {
            if(patientRepository.findByUsername(patient.getUsername()).isPresent() || adminRepository.findByUsername(patient.getUsername()) != null || doctorRepository.findByUsername(patient.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(String.format(ALREADY_EXISTING,patient.getUsername()));
            }

            if(patientRepository.findByPhoneNumber(patient.getPhoneNumber()).isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(String.format(ALREADY_EXISTING,patient.getPhoneNumber()));
            }

            if(patientRepository.findByEmail(patient.getEmail()).isPresent()){
                return ResponseEntity.status(HttpStatus.CONFLICT).body(String.format(ALREADY_EXISTING,patient.getEmail()));
            }

            Patient saved = patientRepository.save(patient);
            logger.info("Patient registered with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            logger.error("Error registering patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(FAILED_REGISTRATION);
        }
    }

    @Override
    public ResponseEntity<?> getAllPatients(int page, int size) {
        try {
            logger.info("Retrieving all patients");
            Pageable pageable = PageRequest.of(page, size);
            Page<PatientDTO> patientPage = patientRepository.findAll(pageable).map(globalMapper::toPatientDTO);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("data",patientPage.getContent());
            response.put("pageNo", pageable.getPageNumber());
            response.put("pageSize", pageable.getPageSize());
            response.put("totalResults", patientPage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving patients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch patients");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updatePatient(PatientDTO updatedPatientDTO, Long patientId, String loggedInUsername) {
        try {
            // Only validate fields that are explicitly present and are null or empty
            if (updatedPatientDTO.getName() != null && updatedPatientDTO.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Name should not be null");
            }
            if (updatedPatientDTO.getEmail() != null && updatedPatientDTO.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email should not be null");
            }
            if (updatedPatientDTO.getPhoneNumber() != null && updatedPatientDTO.getPhoneNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Phone number should not be null");
            }

            if(updatedPatientDTO.getUsername() !=null && updatedPatientDTO.getUsername().trim().isEmpty()){
                throw new IllegalArgumentException("Username should not be null");
            }

            Patient existingPatient = patientRepository.findById(patientId)
                    .orElseThrow(() -> {
                        logger.error("Patient not found with ID: {}", patientId);
                        return new PatientNotFoundException(String.format(PATIENT_NOT_FOUND, patientId));
                    });

            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!(existingPatient.getUsername().equals(loggedInUsername) || isAdmin)) {
                logger.warn("User {} attempted to update another patient: {}", loggedInUsername, existingPatient.getUsername());
                throw new ClinicExceptionHandler.UnauthorizedAccessException(UNAUTHORIZED);
            }

            logger.info("Updating patient data for ID: {}", patientId);

            // Update only non-null properties from DTO to entity
            BeanUtils.copyProperties(updatedPatientDTO, existingPatient, NullPropertyUtils.getNullPropertyNames(updatedPatientDTO));

            Patient savedPatient = patientRepository.save(existingPatient);

            return ResponseEntity.ok(globalMapper.toPatientDTO(savedPatient));
        } catch (ClinicExceptionHandler.UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (PatientNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update patient");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> deletePatientById(Long id) {
        try {
            Optional<Patient> optionalPatient = patientRepository.findById(id);
            if (!optionalPatient.isPresent()) {
                logger.error("Patient not found with ID: {}", id);
                throw new PatientNotFoundException(String.format(PATIENT_NOT_FOUND, id));
            }

            patientRepository.deleteById(id);
            logger.info("Successfully deleted patient with ID: {}", id);

            return ResponseEntity.ok("Patient with ID " + id + " deleted successfully.");
        } catch (PatientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete patient");
        }
    }
}
