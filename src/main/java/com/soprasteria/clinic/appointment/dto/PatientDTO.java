package com.soprasteria.clinic.appointment.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

public class PatientDTO {

	private Long id;

	private String name;

	@Column(unique = true)
	@Email(message = "Please provide a valid email address")
	private String email;

	@Column(unique = true)
	@Pattern(regexp = "^\\d{10}$", message = "Please provide a valid phone number")
	@Size(min = 10, max = 10, message = "Please provide a valid phone number")
	private String phoneNumber;

	private String age;

	@Column(unique = true)
	private String username;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}
}
