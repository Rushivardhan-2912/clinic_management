package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AppointmentController appointmentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testBookAppointment() {
        Long patientId = 1L;
        String username = "patientUser";
        AppointmentDTO dto = new AppointmentDTO();

        when(authentication.getName()).thenReturn(username);
        ResponseEntity<?> expected = ResponseEntity.status(HttpStatus.CREATED).body("Booked");
        when(appointmentService.bookAppointment(dto, patientId, username))
                .thenReturn((ResponseEntity) expected);

        ResponseEntity<?> response = appointmentController.bookAppointment(dto, patientId, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Booked", response.getBody());
        verify(appointmentService, times(1)).bookAppointment(dto, patientId, username);
    }

    @Test
    void testViewAppointmentsForPatient() {
        Long patientId = 1L;
        String username = "patientUser";
        int page = 0, size = 10;

        when(authentication.getName()).thenReturn(username);
        ResponseEntity<?> expected = ResponseEntity.ok("Patient Appointments");
        when(appointmentService.viewAllAppointmentsForPatient(patientId, username, page, size))
                .thenReturn((ResponseEntity) expected);

        ResponseEntity<?> response = appointmentController.viewAppointmentsForPatient(patientId, page, size, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Patient Appointments", response.getBody());
        verify(appointmentService, times(1)).viewAllAppointmentsForPatient(patientId, username, page, size);
    }

    @Test
    void testViewAppointmentsForDoctor() {
        Long doctorId = 2L;
        String username = "doctorUser";
        int page = 0, size = 10;

        when(authentication.getName()).thenReturn(username);
        ResponseEntity<?> expected = ResponseEntity.ok("Doctor Appointments");
        when(appointmentService.viewAllAppointmentsForDoctor(doctorId, username, page, size))
                .thenReturn((ResponseEntity) expected);

        ResponseEntity<?> response = appointmentController.viewAppointmentsForDoctor(doctorId, page, size, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Doctor Appointments", response.getBody());
        verify(appointmentService, times(1)).viewAllAppointmentsForDoctor(doctorId, username, page, size);
    }

    @Test
    void testViewAllAppointments() {
        int page = 0, size = 10;

        ResponseEntity<?> expected = ResponseEntity.ok("All Appointments");
        when(appointmentService.viewAllAppointments(page, size)).thenReturn((ResponseEntity) expected);

        ResponseEntity<?> response = appointmentController.viewAllAppointments(page, size);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("All Appointments", response.getBody());
        verify(appointmentService, times(1)).viewAllAppointments(page, size);
    }

    @Test
    void testUpdateAppointment() {
        Long appointmentId = 5L;
        String username = "patientUser";
        AppointmentDTO dto = new AppointmentDTO();

        when(authentication.getName()).thenReturn(username);
        ResponseEntity<?> expected = ResponseEntity.ok("Updated");
        when(appointmentService.rescheduleAppointment(dto, appointmentId, username))
                .thenReturn((ResponseEntity) expected);

        ResponseEntity<?> response = appointmentController.updateAppointment(dto, appointmentId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody());
        verify(appointmentService, times(1)).rescheduleAppointment(dto, appointmentId, username);
    }

    @Test
    void testCancelAppointment() {
        Long appointmentId = 10L;
        Long patientId = 20L;
        String username = "patientUser";

        when(authentication.getName()).thenReturn(username);
        ResponseEntity<?> expected = ResponseEntity.ok("Canceled");
        when(appointmentService.cancelAppointment(appointmentId, patientId, username))
                .thenReturn((ResponseEntity) expected);

        ResponseEntity<?> response = appointmentController.cancelAppointment(appointmentId, patientId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Canceled", response.getBody());
        verify(appointmentService, times(1)).cancelAppointment(appointmentId, patientId, username);
    }
}
