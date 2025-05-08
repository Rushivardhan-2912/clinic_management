package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface DoctorService {

    ResponseEntity<?> findByUsername(String username);

    ResponseEntity<?> registerDoctor(Doctor doctor);

    ResponseEntity<?> getAllDoctors(int page, int size, Authentication authentication);

    ResponseEntity<?> updateDoctor(DoctorDTO doctorDTO, Long id, Authentication authentication);

    ResponseEntity<?> deleteDoctorById(Long id);
}
