package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AuthRequestDTO;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<?> login(AuthRequestDTO request);
}
