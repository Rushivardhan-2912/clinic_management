package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.AppointmentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

	private static final Logger logger = LogManager.getLogger(AppointmentController.class);

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private PatientRepository patientRepository;

	@PostMapping("/patient/{id}")
	@PreAuthorize("hasRole('PATIENT')")
	public ResponseEntity<?> bookAppointment(@RequestBody AppointmentDTO appointmentDTO,
											 @PathVariable Long id,
											 Authentication authentication) {
		logger.info("Attempting to book appointment for patient ID: {}", id);
		return appointmentService.bookAppointment(appointmentDTO, id, authentication.getName());
	}

	@GetMapping("/patient/{id}")
	@PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
	public ResponseEntity<?> viewAppointmentsForPatient(@PathVariable Long id,
														@RequestParam(defaultValue = "0") int page,
														@RequestParam(defaultValue = "10") int size,
														Authentication authentication) {
		logger.info("Retrieving appointments for patient ID: {} ", id);
		return appointmentService.viewAllAppointmentsForPatient(id, authentication);
	}

	@GetMapping("/doctor/{id}")
	@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
	public ResponseEntity<?> viewAppointmentsForDoctor(@PathVariable Long id,
													   @RequestParam(defaultValue = "0") int page,
													   @RequestParam(defaultValue = "10") int size,
													   Authentication authentication) {
		logger.info("Retrieving appointments for doctor ID: {}", id);
		return appointmentService.viewAllAppointmentsForDoctor(id, authentication);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> viewAllAppointments(@RequestParam(defaultValue = "0") int page,
												 @RequestParam(defaultValue = "10") int size) {
		logger.info("Retrieving all appointments");
		return appointmentService.viewAllAppointments();
	}

	@PutMapping("/{appointmentId}")
	@PreAuthorize("hasRole('PATIENT')")
	public ResponseEntity<?> updateAppointment(@RequestBody AppointmentDTO appointmentDTO,
											   @PathVariable Long appointmentId,
											   Authentication authentication) {
		logger.info("Attempting to update appointment ID: {}", appointmentId);
		return appointmentService.rescheduleAppointment(appointmentDTO, appointmentId, authentication.getName());
	}

	@DeleteMapping("/{appointmentId}/patient/{patientId}")
	@PreAuthorize("hasRole('PATIENT')")
	public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId,
											   @PathVariable Long patientId,
											   Authentication authentication) {
		logger.info("Attempting to cancel appointment ID: {} for patient ID: {}", appointmentId, patientId);
		return appointmentService.cancelAppointment(appointmentId, patientId, authentication);
	}
}
