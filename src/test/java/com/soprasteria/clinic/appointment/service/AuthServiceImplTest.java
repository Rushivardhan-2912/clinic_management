package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.config.CustomUserDetailsService;
import com.soprasteria.clinic.appointment.config.JwtService;
import com.soprasteria.clinic.appointment.dto.AuthRequestDTO;
import com.soprasteria.clinic.appointment.dto.AuthResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import com.soprasteria.clinic.appointment.service.implementation.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("user1");
        request.setPassword("password");

        // Mock authentication success: return a mock Authentication object
        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));

        when(userDetailsService.loadUserByUsername("user1")).thenReturn(userDetails);

        when(jwtService.generateToken(userDetails)).thenReturn("mocked-jwt-token");

        ResponseEntity<?> response = authService.login(request);

        assertEquals(201, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof AuthResponseDTO);
        assertEquals("mocked-jwt-token", ((AuthResponseDTO) response.getBody()).getToken());

        verify(authenticationManager).authenticate(any());
        verify(userDetailsService).loadUserByUsername("user1");
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void testLogin_BadCredentialsException() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("user1");
        request.setPassword("wrong-password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authService.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid Username or Password", response.getBody());

        verify(authenticationManager).authenticate(any());
    }

}
