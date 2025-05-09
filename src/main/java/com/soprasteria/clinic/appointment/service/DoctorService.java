package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import org.springframework.http.ResponseEntity;

public interface DoctorService {

    ResponseEntity<?> findByUsername(String username);

    ResponseEntity<?> registerDoctor(Doctor doctor);

    ResponseEntity<?> getAllDoctors(int page, int size);

    ResponseEntity<?> updateDoctor(DoctorDTO doctorDTO, Long id, String loginUsername);

    ResponseEntity<?> deleteDoctorById(Long id);

}
