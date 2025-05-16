package com.soprasteria.clinic.appointment.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.PatientNotFoundException;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.PatientService;
import com.soprasteria.clinic.appointment.util.NullPropertyUtils;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.soprasteria.clinic.appointment.util.GenericMessages.*;

@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger logger = LogManager.getLogger(PatientServiceImpl.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private GlobalMapper globalMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public ResponseEntity<?> registerPatient(Patient patient) {
        try {
            Patient saved = patientRepository.save(patient);
            logger.info("Patient registered with ID: {}", saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            logger.error("Error registering patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to register patient");
        }
    }

    @Override
    public ResponseEntity<?> getAllPatients(int page, int size) {
        try {
            logger.info("Retrieving all patients");
            Pageable pageable = PageRequest.of(page, size);
            Page<Patient> patientPage = patientRepository.findAll(pageable);
            List<PatientDTO> dtos = patientPage.map(globalMapper::toPatientDTO).getContent();
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            logger.error("Error retrieving patients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch patients");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updatePatient(PatientDTO updatedPatientDTO, Long patientId, String loggedInUsername) {
        try {
            Patient existingPatient = patientRepository.findById(patientId)
                    .orElseThrow(() -> {
                        logger.error("Patient not found with ID: {}", patientId);
                        return new PatientNotFoundException(String.format(PATIENT_NOT_FOUND,patientId));
                    });

            if (!existingPatient.getUsername().equals(loggedInUsername)) {
                logger.warn("User {} attempted to update data for {}", loggedInUsername, existingPatient.getUsername());
                throw new ClinicExceptionHandler.UnauthorizedAccessException(UNAUTHORIZED);
            }

            logger.info("Updating patient data for ID: {}", patientId);
            BeanUtils.copyProperties(updatedPatientDTO, existingPatient, NullPropertyUtils.getNullPropertyNames(updatedPatientDTO));
            Patient savedPatient = patientRepository.save(existingPatient);

            return ResponseEntity.ok(globalMapper.toPatientDTO(savedPatient));
        } catch (ClinicExceptionHandler.UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (PatientNotFoundException e) {
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
                throw new PatientNotFoundException(String.format(PATIENT_NOT_FOUND,id));
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
