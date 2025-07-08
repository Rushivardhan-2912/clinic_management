package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import org.springframework.http.ResponseEntity;

public interface AppointmentService {

    ResponseEntity<?> bookAppointment(AppointmentDTO appointmentDTO, Long id, String loggedInUsername);

    ResponseEntity<?> viewAllAppointmentsForPatient(Long patientId , String loggedInUsername, int page, int size);

    ResponseEntity<?> viewAllAppointmentsForDoctor(Long doctorId, String loggedInUsername,int page,int size);

    ResponseEntity<?> viewAllAppointments(int page, int size);

    ResponseEntity<?> cancelAppointment(Long appointmentId, Long patientId, String loggedInUsername);

    ResponseEntity<?> rescheduleAppointment(AppointmentDTO appointmentDTO, Long appointmentId, String loggedInUsername);
}
