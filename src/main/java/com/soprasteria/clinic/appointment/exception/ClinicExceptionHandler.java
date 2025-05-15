package com.soprasteria.clinic.appointment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ClinicExceptionHandler {

    // Patient Not Found Exception Handler
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<?> handlePatientNotFound(PatientNotFoundException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Doctor Not Found Exception Handler
    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<?> handleDoctorNotFound(DoctorNotFoundException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Appointment Not Found Exception Handler
    @ExceptionHandler(AppointmentNotFoundException.class)
    public ResponseEntity<?> handleAppointmentNotFound(AppointmentNotFoundException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Availability Not Found Exception Handler
    @ExceptionHandler(AvailabilityNotFoundException.class)
    public ResponseEntity<?> handleAvailabilityNotFound(AvailabilityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Admin Not Found Exception Handler
    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<?> handleAdminNotFound(AdminNotFoundException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Invalid Time Slot Exception Handler
    @ExceptionHandler(InvalidTimeSlotException.class)
    public ResponseEntity<?> handleInvalidTimeSlot(InvalidTimeSlotException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Bad Credentials Exception Handler
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    // Unauthorized Access Exception Handler
    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<?> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // General Exception Handler (for unexpected errors)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllOtherExceptions(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + ex.getMessage());
    }

    // Utility method to construct the response
    private ResponseEntity<?> buildResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);  // Only send the message, not the stack trace
        return new ResponseEntity<>(response, status);
    }

    // --- Custom Exception Classes ---

    // Patient Not Found Exception
    public static class PatientNotFoundException extends RuntimeException {
        public PatientNotFoundException(String message) { super(message); }
    }

    // Doctor Not Found Exception
    public static class DoctorNotFoundException extends RuntimeException {
        public DoctorNotFoundException(String message) { super(message); }
    }

    // Appointment Not Found Exception
    public static class AppointmentNotFoundException extends RuntimeException {
        public AppointmentNotFoundException(String message) { super(message); }
    }

    // Availability Not Found Exception
    public static class AvailabilityNotFoundException extends RuntimeException {
        public AvailabilityNotFoundException(String message) { super(message); }
    }

    // Invalid Time Slot Exception
    public static class InvalidTimeSlotException extends RuntimeException {
        public InvalidTimeSlotException(String message) { super(message); }
    }

    // Bad Credentials Exception (for authentication issues)
    public static class BadCredentialsException extends RuntimeException {
        public BadCredentialsException(String message) { super(message); }
    }

    // Unauthorized Access Exception (when users attempt restricted actions)
    public static class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String message) { super(message); }
    }

    // Admin Not Found Exception
    public static class AdminNotFoundException extends RuntimeException {
        public AdminNotFoundException(String message) { super(message); }
    }
}
