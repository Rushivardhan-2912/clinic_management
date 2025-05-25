package com.soprasteria.clinic.appointment.service.implementation;

import com.soprasteria.clinic.appointment.config.CustomUserDetailsService;
import com.soprasteria.clinic.appointment.config.JwtService;
import com.soprasteria.clinic.appointment.dto.AuthRequestDTO;
import com.soprasteria.clinic.appointment.dto.AuthResponseDTO;
import com.soprasteria.clinic.appointment.service.AuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LogManager.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<?> login(AuthRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword())
            );
            logger.info("Attempting to load user : {}",request.getUsername());
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtService.generateToken(userDetails);
            logger.info("Generated Bearer token with username: {}",request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponseDTO(token));

        } catch (Exception ex) {
            logger.error("Loading the user with username : {} and password: {} ",request.getUsername(),request.getPassword());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Username or Password");
        }
    }
}
