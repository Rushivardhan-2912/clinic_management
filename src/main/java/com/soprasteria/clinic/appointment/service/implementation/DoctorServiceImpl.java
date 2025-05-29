package com.soprasteria.clinic.appointment.service.implementation;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.DoctorNotFoundException;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.UnauthorizedAccessException;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AdminRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.DoctorService;
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

import static com.soprasteria.clinic.appointment.util.GenericMessages.*;

@Service
public class DoctorServiceImpl implements DoctorService {

    private static final Logger logger = LogManager.getLogger(DoctorServiceImpl.class);

    private final PatientRepository patientRepository;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final GlobalMapper globalMapper;

    public DoctorServiceImpl(PatientRepository patientRepository, AdminRepository adminRepository, DoctorRepository doctorRepository,
                             GlobalMapper globalMapper) {
        this.patientRepository = patientRepository;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.globalMapper = globalMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<?> registerDoctor(Doctor doctor) {
        try {
            if(patientRepository.findByUsername(doctor.getUsername()).isPresent() || adminRepository.findByUsername(doctor.getUsername()) != null || doctorRepository.findByUsername(doctor.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(String.format(ALREADY_EXISTING,doctor.getUsername()));
            }
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
            Page<DoctorDTO> doctorsPage = doctorRepository.findAll(pageable).map(globalMapper::toDoctorDTO);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("data",doctorsPage.getContent());
            response.put("pageNo", pageable.getPageNumber());
            response.put("pageSize", pageable.getPageSize());
            response.put("totalResults", doctorsPage.getTotalElements());
            return ResponseEntity.ok(response);
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

            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!(isAdmin || existingDoctor.getUsername().equals(loginUsername))) {
                logger.warn("Unauthorized update attempt by user: {}", loginUsername);
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            BeanUtils.copyProperties(updatedDoctorDTO, existingDoctor, NullPropertyUtils.getNullPropertyNames(updatedDoctorDTO));
            Doctor savedDoctor = doctorRepository.save(existingDoctor);

            return ResponseEntity.ok(globalMapper.toDoctorDTO(savedDoctor));
        } catch (DoctorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
        catch (Exception e) {
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting doctor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Deletion failed");
        }
    }
}
