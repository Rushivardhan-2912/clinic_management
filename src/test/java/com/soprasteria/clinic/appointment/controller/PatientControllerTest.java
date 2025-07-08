package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PatientControllerTest {

    @InjectMocks
    private PatientController patientController;

    @Mock
    private PatientService patientService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterPatient_Success() {
        Patient patient = new Patient();
        patient.setUsername("patient1");

        ResponseEntity<?> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(patient);
        when(patientService.registerPatient(patient)).thenReturn((ResponseEntity) expectedResponse);

        ResponseEntity<?> response = patientController.registerPatient(patient);

        assertEquals(expectedResponse, response);
        verify(patientService, times(1)).registerPatient(patient);
    }

    @Test
    void testGetAllPatients_Success() {
        int page = 0, size = 10;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(Collections.emptyList());

        when(patientService.getAllPatients(page, size)).thenReturn((ResponseEntity) expectedResponse);

        ResponseEntity<?> response = patientController.getAllPatients(page, size);

        assertEquals(expectedResponse, response);
        verify(patientService, times(1)).getAllPatients(page, size);
    }

    @Test
    void testUpdatePatient_Success() {
        Long patientId = 1L;
        PatientDTO patientDTO = new PatientDTO();
        String username = "patient1";

        when(authentication.getName()).thenReturn(username);
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(patientDTO);
        when(patientService.updatePatient(patientDTO, patientId, username)).thenReturn((ResponseEntity) expectedResponse);

        ResponseEntity<?> response = patientController.updatePatient(patientDTO, patientId, authentication);

        assertEquals(expectedResponse, response);
        verify(patientService, times(1)).updatePatient(patientDTO, patientId, username);
    }

    @Test
    void testDeletePatientById_Success() {
        Long patientId = 2L;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok("Patient with ID " + patientId + " deleted successfully.");

        when(patientService.deletePatientById(patientId)).thenReturn((ResponseEntity) expectedResponse);

        ResponseEntity<?> response = patientController.deletePatientById(patientId);

        assertEquals(expectedResponse, response);
        verify(patientService, times(1)).deletePatientById(patientId);
    }
}
