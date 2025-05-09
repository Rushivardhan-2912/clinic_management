package com.soprasteria.clinic.appointment.controller;

import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.service.DoctorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

	private static final Logger logger = LogManager.getLogger(DoctorController.class);

	@Autowired
	private DoctorService doctorService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<?> registerDoctor(@RequestBody Doctor doctor) {
		return doctorService.registerDoctor(doctor);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
	public ResponseEntity<?> getAllDoctors(@RequestParam(defaultValue = "0") int page,
										   @RequestParam(defaultValue = "10") int size) {
		return doctorService.getAllDoctors(page, size);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('DOCTOR')")
	public ResponseEntity<?> updateDoctor(@RequestBody DoctorDTO doctorDTO,
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
