package com.soprasteria.clinic.appointment.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.soprasteria.clinic.appointment.util.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Entity
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "doctor_seq")
    @SequenceGenerator(name = "doctor_seq", sequenceName = "doctor_sequence", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Name is mandatory")
    private String doctorName;

    @NotBlank(message = "Specialization is mandatory")
    private String doctorSpecialization;

    @NotBlank(message = "Username is mandatory")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Password is mandatory")
    private String doctorPassword;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role=Role.DOCTOR;

    // One-to-many relationship with Availability
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Availability> doctorAvailabilities;

    // One-to-many relationship with Appointment
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    @JsonManagedReference("appointment-doctor")  // Same unique name as in Appointment entity
    private List<Appointment> doctorAppointments;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDoctorPassword() {
        return doctorPassword;
    }

    public void setDoctorPassword(String doctorPassword) {
        this.doctorPassword = doctorPassword;
    }

    public List<Availability> getDoctorAvailabilities() {
        return doctorAvailabilities;
    }

    public void setDoctorAvailabilities(List<Availability> doctorAvailabilities) {
        this.doctorAvailabilities = doctorAvailabilities;
    }

    public List<Appointment> getDoctorAppointments() {
        return doctorAppointments;
    }

    public void setDoctorAppointments(List<Appointment> doctorAppointments) {
        this.doctorAppointments = doctorAppointments;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
