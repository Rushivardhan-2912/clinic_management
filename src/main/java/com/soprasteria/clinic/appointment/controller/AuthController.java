package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AuthRequestDTO;
import com.soprasteria.clinic.appointment.service.AuthService;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO request) {
        return authService.login(request);
    }
}
