package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface AppointmentService {

    ResponseEntity<?> bookAppointment(AppointmentDTO appointmentDTO, Long id, String loggedInUsername);

    ResponseEntity<?> viewAllAppointmentsForPatient(Long patientId, Authentication authentication);

    ResponseEntity<?> viewAllAppointmentsForDoctor(Long doctorId, Authentication authentication);

    ResponseEntity<?> viewAllAppointments();

    ResponseEntity<?> cancelAppointment(Long appointmentId, Long patientId, Authentication authentication);

    ResponseEntity<?> rescheduleAppointment(AppointmentDTO appointmentDTO, Long appointmentId, String loggedInUsername);
}
