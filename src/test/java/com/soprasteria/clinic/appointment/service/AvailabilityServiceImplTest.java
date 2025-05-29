package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AvailabilityDTO;
import com.soprasteria.clinic.appointment.entity.Availability;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.StatusEnum;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.service.implementation.AvailabilityServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.soprasteria.clinic.appointment.util.GenericMessages.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AvailabilityServiceImplTest {

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private GlobalMapper globalMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private Doctor doctor;
    private Availability availability;
    private AvailabilityDTO availabilityDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup SecurityContextHolder mocks
        SecurityContextHolder.setContext(securityContext);
        doReturn(authentication).when(securityContext).getAuthentication();

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUsername("doctor1");

        availability = new Availability();
        availability.setId(1L);
        availability.setDoctor(doctor);
        availability.setDate(LocalDate.now());
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(10, 0));
        availability.setStatus(StatusEnum.AVAILABLE);

        availabilityDTO = new AvailabilityDTO();
        availabilityDTO.setDate(LocalDate.now());
        availabilityDTO.setStartTime(LocalTime.of(9, 0));
        availabilityDTO.setEndTime(LocalTime.of(10, 0));
        availabilityDTO.setStatus(StatusEnum.AVAILABLE);

        // Default user and role mocks
        doReturn("doctor1").when(authentication).getName();
        GrantedAuthority authority = () -> "ROLE_DOCTOR";
        doReturn(List.<GrantedAuthority>of(authority)).when(authentication).getAuthorities();
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    private void mockRole(String role) {
        GrantedAuthority authority = () -> role;
        doReturn(List.<GrantedAuthority>of(authority)).when(authentication).getAuthorities();
    }

    @Test
    void testAddAvailability_Unauthorized() {
        mockRole("ROLE_DOCTOR");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        ResponseEntity<?> response = availabilityService.addAvailability(availabilityDTO, 1L, "otherUser");

        assertEquals(401, response.getStatusCodeValue());
        assertEquals(UNAUTHORIZED, response.getBody());
    }

    @Test
    void testAddAvailability_DoctorNotFound() {
        mockRole("ROLE_DOCTOR");
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = availabilityService.addAvailability(availabilityDTO, 1L, "doctor1");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().toLowerCase().contains("doctor not found"));
    }

    @Test
    void testAddAvailability_InvalidTime() {
        mockRole("ROLE_DOCTOR");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        availabilityDTO.setStartTime(LocalTime.of(11, 0));
        availabilityDTO.setEndTime(LocalTime.of(10, 0));

        ResponseEntity<?> response = availabilityService.addAvailability(availabilityDTO, 1L, "doctor1");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("End time must be after start time.", response.getBody());
    }

    @Test
    void testAddAvailability_Overlapping() {
        mockRole("ROLE_DOCTOR");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(availabilityRepository.findOverlappingAvailabilities(anyLong(), any(), any(), any()))
                .thenReturn(List.of(availability));

        ResponseEntity<?> response = availabilityService.addAvailability(availabilityDTO, 1L, "doctor1");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Start time must be in the future.", response.getBody());
    }

    @Test
    void testGetAllAvailabilities_Success() {
        mockRole("ROLE_DOCTOR");

        Page<Availability> page = new PageImpl<>(List.of(availability));
        when(availabilityRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(globalMapper.toAvailabilityDTO(any())).thenReturn(availabilityDTO);

        ResponseEntity<?> response = availabilityService.getAllAvailabilities(0, 10);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof Map);

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertTrue(body.containsKey("data"));
        assertTrue(body.get("data") instanceof List);
    }

    @Test
    void testGetDoctorAvailabilities_Success() {
        mockRole("ROLE_DOCTOR");

        Page<Availability> page = new PageImpl<>(List.of(availability));
        when(availabilityRepository.findByDoctorId(eq(1L), any(Pageable.class))).thenReturn(page);
        when(globalMapper.toAvailabilityDTO(any())).thenReturn(availabilityDTO);

        ResponseEntity<?> response = availabilityService.getDoctorAvailabilities(1L, 0, 10);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof Map);
    }

    @Test
    void testUpdateAvailability_Success() {
        mockRole("ROLE_DOCTOR");

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.findOverlappingAvailabilitiesForUpdate(anyLong(), anyLong(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(availabilityRepository.save(any())).thenReturn(availability);
        when(globalMapper.toAvailabilityDTO(any())).thenReturn(availabilityDTO);

        ResponseEntity<?> response = availabilityService.updateAvailability(availabilityDTO, 1L, "doctor1");

        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateAvailability_Unauthorized() {
        mockRole("ROLE_DOCTOR");
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));

        ResponseEntity<?> response = availabilityService.updateAvailability(availabilityDTO, 1L, "otherUser");

        assertEquals(401, response.getStatusCodeValue());
        assertEquals(UNAUTHORIZED, response.getBody());
    }

    @Test
    void testUpdateAvailability_NotFound() {
        mockRole("ROLE_DOCTOR");
        when(availabilityRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = availabilityService.updateAvailability(availabilityDTO, 1L, "doctor1");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().toLowerCase().contains("availability not found"));
    }

    @Test
    void testUpdateAvailability_Overlapping() {
        mockRole("ROLE_DOCTOR");
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(availabilityRepository.findOverlappingAvailabilitiesForUpdate(anyLong(), anyLong(), any(), any(), any()))
                .thenReturn(List.of(availability));

        availabilityDTO.setDate(LocalDate.now());
        availabilityDTO.setStartTime(LocalTime.of(9, 0));
        availabilityDTO.setEndTime(LocalTime.of(10, 0));

        ResponseEntity<?> response = availabilityService.updateAvailability(availabilityDTO, 1L, "doctor1");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Start time must be in the future.", response.getBody());
    }

    @Test
    void addAvailability_shouldReturnInternalServerError_whenExceptionThrown() {
        // Mock doctorRepository to throw RuntimeException to simulate unexpected error
        when(doctorRepository.findById(anyLong())).thenThrow(new RuntimeException("Unexpected Error"));

        // Prepare DTO input
        AvailabilityDTO dto = new AvailabilityDTO();
        dto.setDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(9, 0));
        dto.setEndTime(LocalTime.of(10, 0));
        dto.setStatus(StatusEnum.AVAILABLE);

        ResponseEntity<?> response = availabilityService.addAvailability(dto, 1L, "doctor1");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred while adding availability.", response.getBody());
    }

    @Test
    void updateAvailability_shouldReturnInternalServerError_whenExceptionThrown() {
        when(availabilityRepository.findById(anyLong())).thenThrow(new RuntimeException("Unexpected Error"));

        AvailabilityDTO dto = new AvailabilityDTO();
        dto.setDate(LocalDate.now());
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndTime(LocalTime.of(11, 0));
        dto.setStatus(StatusEnum.AVAILABLE);

        ResponseEntity<?> response = availabilityService.updateAvailability(dto, 1L, "doctor1");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error occurred while adding availability.", response.getBody());
    }

    @Test
    void testDeleteAvailability_Success() {
        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(doctorRepository.findByUsername("doctor1")).thenReturn(Optional.of(doctor));
        doNothing().when(availabilityRepository).deleteById(1L);

        ResponseEntity<?> response = availabilityService.deleteAvailability(1L, "doctor1");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains("Availability deleted successfully"));
    }

    @Test
    void testDeleteAvailability_Unauthorized() {
        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(2L);
        anotherDoctor.setUsername("otherDoc");

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(doctorRepository.findByUsername("otherDoc")).thenReturn(Optional.of(anotherDoctor));

        ResponseEntity<?> response = availabilityService.deleteAvailability(1L, "otherDoc");

        assertEquals(401, response.getStatusCodeValue());
        assertEquals(String.format(UNAUTHORIZED), response.getBody());
    }

    @Test
    void testDeleteAvailability_NotFound() {
        mockRole("ROLE_DOCTOR");

        when(availabilityRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = availabilityService.deleteAvailability(1L, "doctor1");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().toLowerCase().contains("availability not found"));
    }

    @Test
    void testDeleteAvailability_DoctorNotFound() {
        mockRole("ROLE_DOCTOR");

        when(availabilityRepository.findById(1L)).thenReturn(Optional.of(availability));
        when(doctorRepository.findByUsername("doctor1")).thenReturn(Optional.empty());

        ResponseEntity<?> response = availabilityService.deleteAvailability(1L, "doctor1");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().toLowerCase().contains("doctor not found"));
    }


    @Test
    void deleteAvailability_shouldReturnInternalServerError_whenExceptionThrown() {
        when(availabilityRepository.findById(anyLong())).thenThrow(new RuntimeException("Unexpected Error"));

        ResponseEntity<?> response = availabilityService.deleteAvailability(1L, "doctor1");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected Error", response.getBody());
    }
}
