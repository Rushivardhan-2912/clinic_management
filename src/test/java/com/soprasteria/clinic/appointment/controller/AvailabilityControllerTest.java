package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.service.AvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AvailabilityControllerTest {

    @Mock
    private AvailabilityService availabilityService;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AvailabilityController availabilityController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddAvailability() {
        Long doctorId = 1L;
        AvailabilityDTO dto = new AvailabilityDTO();

        when(authentication.getName()).thenReturn("doctorUser");

        ResponseEntity expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(dto);
        when(availabilityService.addAvailability(dto, doctorId, "doctorUser")).thenReturn(expectedResponse);

        ResponseEntity response = availabilityController.addAvailability(dto, doctorId, authentication);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(availabilityService, times(1)).addAvailability(dto, doctorId, "doctorUser");
    }

    @Test
    void testGetAllAvailabilities() {
        ResponseEntity expected = ResponseEntity.ok("List of availabilities");
        when(availabilityService.getAllAvailabilities(0, 10)).thenReturn(expected);

        ResponseEntity response = availabilityController.getAllAvailabilities(0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(availabilityService, times(1)).getAllAvailabilities(0, 10);
    }

    @Test
    void testGetDoctorAvailabilities() {
        Long doctorId = 1L;
        ResponseEntity expected = ResponseEntity.ok("Doctor availabilities");
        when(availabilityService.getDoctorAvailabilities(doctorId, 0, 10)).thenReturn(expected);

        ResponseEntity response = availabilityController.getDoctorAvailabilities(doctorId, 0, 10);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(availabilityService, times(1)).getDoctorAvailabilities(doctorId, 0, 10);
    }

    @Test
    void testUpdateAvailability() {
        Long availabilityId = 1L;
        AvailabilityDTO dto = new AvailabilityDTO();

        when(authentication.getName()).thenReturn("doctorUser");

        ResponseEntity expected = ResponseEntity.ok("Updated");
        when(availabilityService.updateAvailability(dto, availabilityId, "doctorUser")).thenReturn(expected);

        ResponseEntity response = availabilityController.updateAvailability(dto, availabilityId, authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(availabilityService, times(1)).updateAvailability(dto, availabilityId, "doctorUser");
    }

    @Test
    void testDeleteAvailability() {
        Long availabilityId = 1L;
        String username = "doctorUser";

        when(authentication.getName()).thenReturn(username);
        ResponseEntity expected = ResponseEntity.ok("Deleted");
        when(availabilityService.deleteAvailability(availabilityId, username)).thenReturn(expected);

        ResponseEntity response = availabilityController.deleteAvailability(availabilityId, authentication);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(availabilityService, times(1)).deleteAvailability(availabilityId, username);
    }
}
