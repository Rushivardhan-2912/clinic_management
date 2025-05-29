package com.soprasteria.clinic.appointment.config;

import com.soprasteria.clinic.appointment.entity.Admin;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.repo.AdminRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsername_AdminFound() {
        Admin admin = new Admin("adminUser", "adminPass");
        when(adminRepository.findByUsername("adminUser")).thenReturn(admin);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("adminUser");

        assertEquals("adminUser", userDetails.getUsername());
        assertEquals("adminPass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testLoadUserByUsername_DoctorFound() {
        Doctor doctor = new Doctor();
        doctor.setUsername("doctorUser");
        doctor.setPassword("doctorPass");

        when(adminRepository.findByUsername("doctorUser")).thenReturn(null);
        when(doctorRepository.findByUsername("doctorUser")).thenReturn(Optional.of(doctor));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("doctorUser");

        assertEquals("doctorUser", userDetails.getUsername());
        assertEquals("doctorPass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR")));
    }

    @Test
    void testLoadUserByUsername_PatientFound() {
        Patient patient = new Patient();
        patient.setUsername("patientUser");
        patient.setPassword("patientPass");

        when(adminRepository.findByUsername("patientUser")).thenReturn(null);
        when(doctorRepository.findByUsername("patientUser")).thenReturn(Optional.empty());
        when(patientRepository.findByUsername("patientUser")).thenReturn(Optional.of(patient));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("patientUser");

        assertEquals("patientUser", userDetails.getUsername());
        assertEquals("patientPass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PATIENT")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        String username = "unknownUser";

        when(adminRepository.findByUsername(username)).thenReturn(null);
        when(doctorRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(patientRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(username)
        );

        assertEquals(String.format("User with ID '%s' not found", username), exception.getMessage());
    }
}
