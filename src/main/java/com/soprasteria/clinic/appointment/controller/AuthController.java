package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AuthRequestDTO;
import com.soprasteria.clinic.appointment.service.AuthService;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {
        logger.info("Attempting to generate token with username: {} and password: {} ", request.getUsername(),request.getPassword());
        return authService.login(request);
    }
}
