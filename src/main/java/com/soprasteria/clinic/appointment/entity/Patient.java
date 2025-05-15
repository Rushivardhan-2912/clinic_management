package com.soprasteria.clinic.appointment.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.soprasteria.clinic.appointment.util.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.util.List;

@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_seq")
    @SequenceGenerator(name = "patient_seq", sequenceName = "patient_sequence", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    @NotBlank(message = "Patient email is required")
    @Column(unique = true)
    @Email(message = "Please provide a valid email address")
    private String patientEmail;

    @NotBlank(message = "Patient phone number is required")
    @Column(unique = true)
    @Pattern(regexp = "^[0-9]{10}$", message = "Please provide a valid phone number")
    @Size(min = 10, max = 10, message = "Please provide a valid phone number")
    private String patientPhoneNumber;

    @NotBlank(message = "Age is mandatory")
    @Pattern(regexp = "^\\d{1,3}$", message = "Age must be between 1 and 3 digits")
    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 120, message = "Age must not exceed 120")
    private String patientAge;

    @NotBlank(message = "Patient username is required")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Password is mandatory")
    private String patientPassword;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.PATIENT;

    // One-to-many relationship with Appointment
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonManagedReference("appointment-patient")  // Same unique name as in Appointment entity
    private List<Appointment> patientAppointments;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientPhoneNumber() {
        return patientPhoneNumber;
    }

    public void setPatientPhoneNumber(String patientPhoneNumber) {
        this.patientPhoneNumber = patientPhoneNumber;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPatientPassword() {
        return patientPassword;
    }

    public void setPatientPassword(String patientPassword) {
        this.patientPassword = patientPassword;
    }

    public List<Appointment> getPatientAppointments() {
        return patientAppointments;
    }

    public void setPatientAppointments(List<Appointment> patientAppointments) {
        this.patientAppointments = patientAppointments;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
