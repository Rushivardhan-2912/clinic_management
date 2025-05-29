package com.soprasteria.clinic.appointment.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.soprasteria.clinic.appointment.util.GenericMessages.RESOURCE_NOT_FOUND;

@RestControllerAdvice
public class ClinicExceptionHandler {

    // Patient Not Found Exception Handler
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<?> handlePatientNotFound(PatientNotFoundException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArg(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Get the first validation error message
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Invalid input");

        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleInvalidFormat(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException cause) {
            Class<?> targetType = cause.getTargetType();

            if (targetType.equals(java.time.LocalDate.class)) {
                return ResponseEntity.badRequest()
                        .body("Invalid date format: Day must not exceed 31. Please use yyyy-MM-dd.");
            }

            if (targetType.equals(java.time.LocalTime.class)) {
                return ResponseEntity.badRequest()
                        .body("Invalid time format: Please use HH:mm:ss with valid hour (0–23), minute (0–59), and second (0–59).");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid request: " + ex.getMessage());
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
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Invalid Time Slot Exception Handler
    @ExceptionHandler(InvalidTimeSlotException.class)
    public ResponseEntity<?> handleInvalidTimeSlot(InvalidTimeSlotException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(KeyLoadingException.class)
    public ResponseEntity<?> handleJwtException(KeyLoadingException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    // Bad Credentials Exception Handler
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    // General Exception Handler (for unexpected errors)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllOtherExceptions(Exception ex) {
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You don't have permission to access this resource.");
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + ex.getMessage());
    }


    // Utility method to construct the response
    private ResponseEntity<?> buildResponse(HttpStatus status, String message) {
        // Only send the message, not the stack trace
        return ResponseEntity.status(status).body(message);
    }

    // --- Custom Exception Classes ---

    // Patient Not Found Exception
    public static class PatientNotFoundException extends RuntimeException {
        public PatientNotFoundException(String message) {
            super(message);
        }
    }

    // Doctor Not Found Exception
    public static class DoctorNotFoundException extends RuntimeException {
        public DoctorNotFoundException(String message) {
            super(message);
        }
    }

    // Appointment Not Found Exception
    public static class AppointmentNotFoundException extends RuntimeException {
        public AppointmentNotFoundException(String message) {
            super(message);
        }
    }

    // Availability Not Found Exception
    public static class AvailabilityNotFoundException extends RuntimeException {
        public AvailabilityNotFoundException(String message) {
            super(message);
        }
    }

    // Invalid Time Slot Exception
    public static class InvalidTimeSlotException extends RuntimeException {
        public InvalidTimeSlotException(String message) {
            super(message);
        }
    }

    // Bad Credentials Exception (for authentication issues)
    public static class BadCredentialsException extends RuntimeException {
        public BadCredentialsException(String message) {
            super(message);
        }
    }

    // Unauthorized Access Exception (when users attempt restricted actions)
    public static class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }

    public static class KeyLoadingException extends RuntimeException {
        public KeyLoadingException(String message, Throwable cause) {
            super(message, cause);
        }

        public KeyLoadingException(String message) {
            super(message);
        }
    }

}
