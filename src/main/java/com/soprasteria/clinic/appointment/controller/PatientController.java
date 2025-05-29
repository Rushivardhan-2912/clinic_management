package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.PatientDTO;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.service.PatientService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.base.path}/patients")
public class PatientController {

	private static final Logger logger = LogManager.getLogger(PatientController.class);

	private final PatientService patientService;

	public PatientController(PatientService patientService) {
		this.patientService = patientService;
	}

	@PostMapping
	public ResponseEntity<?> registerPatient(@Valid @RequestBody Patient patient) {
		logger.info("Attempting to register new patient with username: {}", patient.getUsername());
		return patientService.registerPatient(patient);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement( name = "bearerAuth")
	public ResponseEntity<?> getAllPatients(@RequestParam(defaultValue = "0") int page,
											@RequestParam(defaultValue = "10") int size) {
		logger.info("Retrieving all patients with page: {} and size: {}", page, size);
		return patientService.getAllPatients(page, size);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
	@SecurityRequirement( name = "bearerAuth")
	public ResponseEntity<?> updatePatient(@Valid @RequestBody PatientDTO patientDTO,
										   @PathVariable Long id,
										   Authentication authentication) {
		logger.info("Attempting to update patient with ID: {} by user: {}", id, authentication.getName());
		return patientService.updatePatient(patientDTO, id, authentication.getName());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@SecurityRequirement( name = "bearerAuth")
	public ResponseEntity<?> deletePatientById(@PathVariable Long id) {
		logger.info("Attempting to delete patient with ID: {}", id);
		return patientService.deletePatientById(id);
	}
}
