package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DoctorControllerTest {

    @InjectMocks
    private DoctorController doctorController;

    @Mock
    private DoctorService doctorService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterDoctor_Success() {
        Doctor doctor = new Doctor();
        doctor.setUsername("doc1");

        ResponseEntity<?> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(doctor);
        when(doctorService.registerDoctor(doctor)).thenReturn((ResponseEntity) expectedResponse);

        ResponseEntity<?> response = doctorController.registerDoctor(doctor);

        assertEquals(expectedResponse, response);
        verify(doctorService, times(1)).registerDoctor(doctor);
    }

    @Test
    void testGetAllDoctors_Success() {
        int page = 0, size = 10;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(Collections.emptyList());

        when(doctorService.getAllDoctors(page, size)).thenReturn((ResponseEntity) expectedResponse);

        ResponseEntity<?> response = doctorController.getAllDoctors(page, size);

        assertEquals(expectedResponse, response);
        verify(doctorService, times(1)).getAllDoctors(page, size);
    }

    @Test
    void testUpdateDoctor_Success() {
        Long doctorId = 1L;
        DoctorDTO doctorDTO = new DoctorDTO();
        String username = "doctor1";

        when(authentication.getName()).thenReturn(username);
        ResponseEntity<?> expectedResponse = ResponseEntity.ok(doctorDTO);
        when(doctorService.updateDoctor(doctorDTO, doctorId, username)).thenReturn((ResponseEntity) expectedResponse);

        ResponseEntity<?> response = doctorController.updateDoctor(doctorDTO, doctorId, authentication);

        assertEquals(expectedResponse, response);
        verify(doctorService, times(1)).updateDoctor(doctorDTO, doctorId, username);
    }

    @Test
    void testDeleteDoctorById_Success() {
        Long doctorId = 2L;
        ResponseEntity<?> expectedResponse = ResponseEntity.ok("Doctor with ID " + doctorId + " deleted successfully.");

        when(doctorService.deleteDoctorById(doctorId)).thenReturn((ResponseEntity) expectedResponse);

        ResponseEntity<?> response = doctorController.deleteDoctorById(doctorId);

        assertEquals(expectedResponse, response);
        verify(doctorService, times(1)).deleteDoctorById(doctorId);
    }
}
