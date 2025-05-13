package com.soprasteria.clinic.appointment.entity;

import com.soprasteria.clinic.appointment.util.Role;
import jakarta.persistence.*;

@Entity
public class Admin {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String username;
	private String password;

	@Enumerated(EnumType.STRING)
	private Role role = Role.ADMIN;

	// Constructors
	public Admin() {
	}

	public Admin(String username, String password) {
		this.username = username;
		this.password = password;
		this.role = Role.ADMIN;
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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}
