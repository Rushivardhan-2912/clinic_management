package com.soprasteria.clinic.appointment.service.implementation;

import com.soprasteria.clinic.appointment.config.CustomUserDetailsService;
import com.soprasteria.clinic.appointment.config.JwtService;
import com.soprasteria.clinic.appointment.dto.AuthRequest;
import com.soprasteria.clinic.appointment.dto.AuthResponse;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler;
import com.soprasteria.clinic.appointment.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Override
    public ResponseEntity<?> login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(token));

        } catch (ClinicExceptionHandler.BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Authentication failed: " + ex.getMessage());
        }
    }
}
