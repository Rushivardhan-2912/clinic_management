package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.service.AppointmentService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointments")
@SecurityRequirement( name = "bearerAuth")
public class AppointmentController {

	private static final Logger logger = LogManager.getLogger(AppointmentController.class);

	private final AppointmentService appointmentService;

	public AppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@PostMapping("/patient/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
	public ResponseEntity<?> bookAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO,
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
		return appointmentService.viewAllAppointmentsForPatient(id, authentication.getName(),page,size);
	}

	@GetMapping("/doctor/{id}")
	@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
	public ResponseEntity<?> viewAppointmentsForDoctor(@Valid @PathVariable Long id,
													   @RequestParam(defaultValue = "0") int page,
													   @RequestParam(defaultValue = "10") int size,
													   Authentication authentication) {
		logger.info("Retrieving appointments for doctor ID: {}", id);
		return appointmentService.viewAllAppointmentsForDoctor(id, authentication.getName(),page,size);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> viewAllAppointments(@RequestParam(defaultValue = "0") int page,
												 @RequestParam(defaultValue = "10") int size) {
		logger.info("Retrieving all appointments");
		return appointmentService.viewAllAppointments(page,size);
	}

	@PutMapping("/{appointmentId}")
	@PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
	public ResponseEntity<?> updateAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO,
											   @PathVariable Long appointmentId,
											   Authentication authentication) {
		logger.info("Attempting to update appointment ID: {}", appointmentId);
		return appointmentService.rescheduleAppointment(appointmentDTO, appointmentId, authentication.getName());
	}

	@DeleteMapping("/{appointmentId}/patient/{patientId}")
	@PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
	public ResponseEntity<?> cancelAppointment(@PathVariable Long appointmentId,
											   @PathVariable Long patientId,
											   Authentication authentication) {
		logger.info("Attempting to cancel appointment ID: {} for patient ID: {}", appointmentId, patientId);
		return appointmentService.cancelAppointment(appointmentId, patientId, authentication.getName());
	}
}
