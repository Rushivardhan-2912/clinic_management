package com.soprasteria.clinic.appointment.entity;

import jakarta.persistence.*;

@Entity
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String username;
	private String password;

	@Column(nullable = true) // make it nullable temporarily
	@Enumerated(EnumType.STRING)
	private RoleEnum role = RoleEnum.ADMIN;

	// Constructors
	public Admin() {
	}

	public Admin(String username, String password) {
		this.username = username;
		this.password = password;
		this.role = RoleEnum.ADMIN;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public RoleEnum getRole() {
		return role;
	}

	public void setRole(RoleEnum roleEnum) {
		this.role = roleEnum;
	}
}
