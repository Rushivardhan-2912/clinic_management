package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Admin;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AdminRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.implementation.PatientServiceImpl;
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
import static com.soprasteria.clinic.appointment.util.GenericMessages.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

class PatientServiceImplTest {

    @InjectMocks
    private PatientServiceImpl patientService;

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

    private Patient patient;
    private PatientDTO patientDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Sample Patient and DTO
        patient = new Patient();
        patient.setId(1L);
        patient.setUsername("rushi123");
        patient.setPhoneNumber("9876543210");
        patient.setEmail("rushi@example.com");
        patient.setName("Rushi");
        patient.setPassword("pass");
        patient.setAge("25");

        patientDTO = new PatientDTO();
        patientDTO.setId(1L);
        patientDTO.setUsername("rushi123");
        patientDTO.setPhoneNumber("9876543210");
        patientDTO.setEmail("rushi@example.com");
        patientDTO.setName("Rushi");
        patientDTO.setAge("25");

        // Mock Spring Security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Authorities - default to admin role
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        doReturn(authorities).when(authentication).getAuthorities();
        doReturn("rushi123").when(authentication).getName();
    }

    @Test
    void testRegisterPatient_Success() {
        when(patientRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());
        when(adminRepository.findByUsername(patient.getUsername())).thenReturn(null);
        when(doctorRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());
        when(patientRepository.findByPhoneNumber(patient.getPhoneNumber())).thenReturn(Optional.empty());
        when(patientRepository.findByEmail(patient.getEmail())).thenReturn(Optional.empty());
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        var response = patientService.registerPatient(patient);

        assertEquals(CREATED, response.getStatusCode());
        assertEquals(patient, response.getBody());
    }

    @Test
    void testRegisterPatient_UsernameConflict_PatientRepository() {
        when(patientRepository.findByUsername(patient.getUsername())).thenReturn(Optional.of(patient));

        var response = patientService.registerPatient(patient);

        assertEquals(CONFLICT, response.getStatusCode());
        assertEquals(String.format(ALREADY_EXISTING, patient.getUsername()), response.getBody());
    }

    @Test
    void testRegisterPatient_UsernameConflict_AdminRepository() {
        when(patientRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());

        Admin admin = new Admin();
        when(adminRepository.findByUsername(patient.getUsername())).thenReturn(admin);

        when(doctorRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());

        var response = patientService.registerPatient(patient);

        assertEquals(CONFLICT, response.getStatusCode());
        assertEquals(String.format(ALREADY_EXISTING, patient.getUsername()), response.getBody());
    }

    @Test
    void testRegisterPatient_UsernameConflict_DoctorRepository() {
        when(patientRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());
        when(adminRepository.findByUsername(patient.getUsername())).thenReturn(null);

        Doctor doctor = new Doctor();
        when(doctorRepository.findByUsername(patient.getUsername())).thenReturn(Optional.of(doctor));

        var response = patientService.registerPatient(patient);

        assertEquals(CONFLICT, response.getStatusCode());
        assertEquals(String.format(ALREADY_EXISTING, patient.getUsername()), response.getBody());
    }

    @Test
    void testRegisterPatient_PhoneNumberConflict() {
        when(patientRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());
        when(adminRepository.findByUsername(patient.getUsername())).thenReturn(null);
        when(doctorRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());
        when(patientRepository.findByPhoneNumber(patient.getPhoneNumber())).thenReturn(Optional.of(patient));

        var response = patientService.registerPatient(patient);

        assertEquals(CONFLICT, response.getStatusCode());
        assertEquals(String.format(ALREADY_EXISTING, patient.getPhoneNumber()), response.getBody());
    }

    @Test
    void testRegisterPatient_EmailConflict() {
        when(patientRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());
        when(adminRepository.findByUsername(patient.getUsername())).thenReturn(null);
        when(doctorRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());
        when(patientRepository.findByPhoneNumber(patient.getPhoneNumber())).thenReturn(Optional.empty());
        when(patientRepository.findByEmail(patient.getEmail())).thenReturn(Optional.of(patient));

        var response = patientService.registerPatient(patient);

        assertEquals(CONFLICT, response.getStatusCode());
        assertEquals(String.format(ALREADY_EXISTING, patient.getEmail()), response.getBody());
    }

    @Test
    void testRegisterPatient_Exception() {
        when(patientRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());
        when(adminRepository.findByUsername(patient.getUsername())).thenReturn(null);
        when(doctorRepository.findByUsername(patient.getUsername())).thenReturn(Optional.empty());
        when(patientRepository.findByPhoneNumber(patient.getPhoneNumber())).thenReturn(Optional.empty());
        when(patientRepository.findByEmail(patient.getEmail())).thenReturn(Optional.empty());

        when(patientRepository.save(any(Patient.class))).thenThrow(new RuntimeException("Database error"));

        var response = patientService.registerPatient(patient);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(FAILED_REGISTRATION, response.getBody());
    }

    @Test
    void testGetAllPatients_Success() {
        Page<Patient> page = new PageImpl<>(List.of(patient));
        when(patientRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(globalMapper.toPatientDTO(patient)).thenReturn(patientDTO);

        var response = patientService.getAllPatients(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("data"));

        @SuppressWarnings("unchecked")
        List<PatientDTO> patientDTOs = (List<PatientDTO>) body.get("data");
        assertEquals(1, patientDTOs.size());
        assertEquals(patientDTO, patientDTOs.get(0));
    }

    @Test
    void testGetAllPatients_Exception() {
        when(patientRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("DB error"));

        var response = patientService.getAllPatients(0, 10);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to fetch patients", response.getBody());
    }

    @Test
    void testUpdatePatient_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(globalMapper.toPatientDTO(patient)).thenReturn(patientDTO);

        var response = patientService.updatePatient(patientDTO, 1L, "rushi123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(patientDTO, response.getBody());
    }

    @Test
    void testUpdatePatient_Unauthorized() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        // Replace authorities with non-admin
        Collection<GrantedAuthority> userAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(userAuthorities).when(authentication).getAuthorities();
        doReturn("someone_else").when(authentication).getName();

        var response = patientService.updatePatient(patientDTO, 1L, "someone_else");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(UNAUTHORIZED, response.getBody());
    }

    @Test
    void testUpdatePatient_NotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        var response = patientService.updatePatient(patientDTO, 1L, "rushi123");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(String.format(PATIENT_NOT_FOUND, "1"), response.getBody());
    }

    @Test
    void testUpdatePatient_Exception() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenThrow(new RuntimeException("Update error"));

        var response = patientService.updatePatient(patientDTO, 1L, "rushi123");

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to update patient", response.getBody());
    }

    @Test
    void testDeletePatientById_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        doNothing().when(patientRepository).deleteById(1L);

        var response = patientService.deletePatientById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Patient with ID 1 deleted successfully.", response.getBody());
    }

    @Test
    void testDeletePatientById_NotFound() {
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        var response = patientService.deletePatientById(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(String.format(PATIENT_NOT_FOUND, "1"), response.getBody());
    }

    @Test
    void testDeletePatientById_Exception() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        doThrow(new RuntimeException("DB error")).when(patientRepository).deleteById(1L);

        var response = patientService.deletePatientById(1L);

        assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to delete patient", response.getBody());
    }
}
