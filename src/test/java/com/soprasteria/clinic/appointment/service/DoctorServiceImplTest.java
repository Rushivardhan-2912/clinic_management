package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AdminRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.implementation.DoctorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static com.soprasteria.clinic.appointment.util.GenericMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorServiceImplTest {

    @InjectMocks
    private DoctorServiceImpl doctorService;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private GlobalMapper globalMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private Doctor doctor;
    private DoctorDTO doctorDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Sample Doctor and DTO
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUsername("docuser");
        doctor.setName("Dr. Strange");
        doctor.setPassword("secret");

        doctorDTO = new DoctorDTO();
        doctorDTO.setId(1L);
        doctorDTO.setUsername("docuser");
        doctorDTO.setName("Dr. Strange");

        // Mock Spring Security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Default Authorities = ROLE_ADMIN
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        doReturn(authorities).when(authentication).getAuthorities();
        doReturn("docuser").when(authentication).getName();
    }

    @Test
    void testRegisterDoctor_Success() {
        when(patientRepository.findByUsername(doctor.getUsername())).thenReturn(Optional.empty());
        when(adminRepository.findByUsername(doctor.getUsername())).thenReturn(null);
        when(doctorRepository.findByUsername(doctor.getUsername())).thenReturn(Optional.empty());
        when(doctorRepository.save(doctor)).thenReturn(doctor);

        var response = doctorService.registerDoctor(doctor);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(doctor, response.getBody());
    }

    @Test
    void testRegisterDoctor_Conflict() {
        when(patientRepository.findByUsername(doctor.getUsername())).thenReturn(Optional.of(new Patient()));

        var response = doctorService.registerDoctor(doctor);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(doctor.getUsername()));
    }

    @Test
    void testRegisterDoctor_Exception() {
        when(patientRepository.findByUsername(doctor.getUsername())).thenReturn(Optional.empty());
        when(adminRepository.findByUsername(doctor.getUsername())).thenReturn(null);
        when(doctorRepository.findByUsername(doctor.getUsername())).thenReturn(Optional.empty());
        when(doctorRepository.save(any(Doctor.class))).thenThrow(new RuntimeException("DB error"));

        var response = doctorService.registerDoctor(doctor);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Registration failed", response.getBody());
    }

    @Test
    void testGetAllDoctors_Success() {
        Page<Doctor> page = new PageImpl<>(List.of(doctor));
        when(doctorRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(globalMapper.toDoctorDTO(doctor)).thenReturn(doctorDTO);

        var response = doctorService.getAllDoctors(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("data"));

        @SuppressWarnings("unchecked")
        List<DoctorDTO> doctors = (List<DoctorDTO>) body.get("data");
        assertEquals(1, doctors.size());
        assertEquals(doctorDTO, doctors.get(0));
    }

    @Test
    void testGetAllDoctors_Exception() {
        when(doctorRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("DB error"));

        var response = doctorService.getAllDoctors(0, 10);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to retrieve doctors", response.getBody());
    }

    @Test
    void testUpdateDoctor_Success_AsAdmin() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(globalMapper.toDoctorDTO(doctor)).thenReturn(doctorDTO);

        // Authenticated user is admin - already mocked in setUp

        var response = doctorService.updateDoctor(doctorDTO, 1L, "docuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(doctorDTO, response.getBody());
    }

    @Test
    void testUpdateDoctor_Success_AsSameUser() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);
        when(globalMapper.toDoctorDTO(doctor)).thenReturn(doctorDTO);

        // Change auth to ROLE_DOCTOR and username = doctor.username
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"));
        doReturn(authorities).when(authentication).getAuthorities();
        doReturn(doctor.getUsername()).when(authentication).getName();

        var response = doctorService.updateDoctor(doctorDTO, 1L, doctor.getUsername());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(doctorDTO, response.getBody());
    }

    @Test
    void testUpdateDoctor_Unauthorized() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // User with no admin role and different username
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_DOCTOR"));
        doReturn(authorities).when(authentication).getAuthorities();
        doReturn("otheruser").when(authentication).getName();

        var response = doctorService.updateDoctor(doctorDTO, 1L, "otheruser");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof ClinicExceptionHandler.UnauthorizedAccessException);
    }

    @Test
    void testUpdateDoctor_NotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        var response = doctorService.updateDoctor(doctorDTO, 1L, "docuser");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(String.format(DOCTOR_NOT_FOUND, 1L)));
    }

    @Test
    void testUpdateDoctor_Exception() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorRepository.save(any(Doctor.class))).thenThrow(new RuntimeException("DB error"));

        var response = doctorService.updateDoctor(doctorDTO, 1L, "docuser");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Update failed", response.getBody());
    }

    @Test
    void testDeleteDoctorById_Success() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        doNothing().when(doctorRepository).deleteById(1L);

        var response = doctorService.deleteDoctorById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Doctor with ID 1 deleted successfully.", response.getBody());
    }

    @Test
    void testDeleteDoctorById_NotFound() {
        when(doctorRepository.existsById(1L)).thenReturn(false);

        var response = doctorService.deleteDoctorById(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(String.format(DOCTOR_NOT_FOUND, 1L)));
    }

    @Test
    void testDeleteDoctorById_Exception() {
        when(doctorRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("DB error")).when(doctorRepository).deleteById(1L);

        var response = doctorService.deleteDoctorById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Deletion failed", response.getBody());
    }

}
