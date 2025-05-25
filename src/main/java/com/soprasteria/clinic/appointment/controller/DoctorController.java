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

@RestController
@RequestMapping("/api/v1/doctors")
@SecurityRequirement( name = "bearerAuth")
public class DoctorController {

	private final DoctorService doctorService;

	public DoctorController(DoctorService doctorService) {
		this.doctorService = doctorService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> registerDoctor(@Valid @RequestBody Doctor doctor) {
		return doctorService.registerDoctor(doctor);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
	public ResponseEntity<?> getAllDoctors(@RequestParam(defaultValue = "0") int page,
										   @RequestParam(defaultValue = "10") int size) {
		return doctorService.getAllDoctors(page, size);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
	public ResponseEntity<?> updateDoctor(@Valid @RequestBody DoctorDTO doctorDTO,
										  @PathVariable Long id,
										  Authentication authentication) {
		return doctorService.updateDoctor(doctorDTO, id, authentication.getName());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> deleteDoctorById(@PathVariable Long id) {
		return doctorService.deleteDoctorById(id);
	}
}
