package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AuthRequestDTO;
import com.soprasteria.clinic.appointment.dto.AuthResponseDTO;
import com.soprasteria.clinic.appointment.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("user1");
        request.setPassword("password");

        AuthResponseDTO authResponseDTO = new AuthResponseDTO("fake-jwt-token");
        ResponseEntity<AuthResponseDTO> responseEntity = ResponseEntity.ok(authResponseDTO);

        // Cast to raw ResponseEntity to fix generic mismatch warning/error in Mockito
        when(authService.login(any(AuthRequestDTO.class))).thenReturn((ResponseEntity) responseEntity);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponseDTO);
        assertEquals("fake-jwt-token", ((AuthResponseDTO) response.getBody()).getToken());

        verify(authService, times(1)).login(any(AuthRequestDTO.class));
    }

    @Test
    void testLoginFailureUnauthorized() {
        AuthRequestDTO request = new AuthRequestDTO();
        request.setUsername("user1");
        request.setPassword("wrongpassword");

        ResponseEntity<String> failureResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad credentials");

        // Cast to raw ResponseEntity here as well
        when(authService.login(any(AuthRequestDTO.class))).thenReturn((ResponseEntity) failureResponse);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Bad credentials", response.getBody());

        verify(authService, times(1)).login(any(AuthRequestDTO.class));
    }

}
