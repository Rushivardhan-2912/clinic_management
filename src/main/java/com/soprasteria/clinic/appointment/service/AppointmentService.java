package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AppointmentService {

    ResponseEntity<?> bookAppointment(AppointmentDTO appointmentDTO, Long id, Authentication authentication);

    ResponseEntity<?> viewAllAppointmentsForPatient(Long patientId, int page, int size, Authentication authentication);

    ResponseEntity<?> viewAllAppointmentsForDoctor(Long doctorId, int page, int size, Authentication authentication);

    ResponseEntity<?> viewAllAppointments(int page, int size);

    ResponseEntity<?> cancelAppointment(Long appointmentId, Long patientId, Authentication authentication);

    ResponseEntity<?> rescheduleAppointment(AppointmentDTO appointmentDTO, Long appointmentId, String loggedInUsername);
}
