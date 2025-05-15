package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;
import org.springframework.http.ResponseEntity;

public interface PatientService {

    ResponseEntity<?> registerPatient(Patient patient);

    ResponseEntity<?> getAllPatients(int page, int size);

    ResponseEntity<?> updatePatient(PatientDTO updatedPatientDTO, Long id, String loggedInUsername);

    ResponseEntity<?> deletePatientById(Long id);
}
