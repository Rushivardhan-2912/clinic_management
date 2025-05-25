package com.soprasteria.clinic.appointment.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

public class DoctorDTO {

	private Long id;

	private String name;

	private String specialization;

	@Column(unique = true)
	private String username;

	public DoctorDTO() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
