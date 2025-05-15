package com.soprasteria.clinic.appointment.service.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.DoctorNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.UnauthorizedAccessException;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.service.DoctorService;
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

import static com.soprasteria.clinic.appointment.util.GenericMessages.DOCTOR_NOT_FOUND;
import static com.soprasteria.clinic.appointment.util.GenericMessages.UNAUTHORIZED;

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
    public ResponseEntity<?> getAllDoctors(int page, int size) {
        try {
            logger.info("Fetching all doctors - page: {}, size: {}", page, size);
            Pageable pageable = PageRequest.of(page, size);
            Page<Doctor> doctorsPage = doctorRepository.findAll(pageable);
            List<DoctorDTO> doctors = doctorsPage.map(globalMapper::toDoctorDTO).getContent();
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            logger.error("Error retrieving doctor list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve doctors");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> updateDoctor(DoctorDTO updatedDoctorDTO, Long doctorId, String loginUsername) {
        try {
            Doctor existingDoctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> {
                        logger.error("Doctor not found with ID: {}", doctorId);
                        return new DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND,doctorId));
                    });

            if (!existingDoctor.getUsername().equals(loginUsername)) {
                logger.warn("Unauthorized update attempt by user: {}", loginUsername);
                return new ResponseEntity<>(new UnauthorizedAccessException(UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
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
                throw new ClinicExceptionHandler.DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND,id));
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
