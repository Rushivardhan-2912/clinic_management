package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.service.DoctorService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("${api.base.path}/doctors")
@SecurityRequirement( name = "bearerAuth")
public class DoctorController {

	private static final Logger logger= LogManager.getLogger(DoctorController.class);

	private final DoctorService doctorService;

	public DoctorController(DoctorService doctorService) {
		this.doctorService = doctorService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> registerDoctor(@Valid @RequestBody Doctor doctor) {
		logger.info("Attempting to add doctor with username : {}",doctor.getUsername());
		return doctorService.registerDoctor(doctor);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
	public ResponseEntity<?> getAllDoctors(@RequestParam(defaultValue = "0") int page,
										   @RequestParam(defaultValue = "10") int size) {
		logger.info("Retriving All the doctors! ");
		return doctorService.getAllDoctors(page, size);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
	public ResponseEntity<?> updateDoctor(@Valid @RequestBody DoctorDTO doctorDTO,
										  @PathVariable Long id,
										  Authentication authentication) {
		logger.info("Attempting to update details of doctor with id : {}", id);
		return doctorService.updateDoctor(doctorDTO, id, authentication.getName());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteDoctorById(@PathVariable Long id) {
		logger.info("Deleting the doctor with id: {}",id);
		return doctorService.deleteDoctorById(id);
	}
}