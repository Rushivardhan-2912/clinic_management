package com.soprasteria.clinic.appointment.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_seq")
    @SequenceGenerator(name = "patient_seq", sequenceName = "patient_sequence", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Patient name is required")
    private String name;

    @NotBlank(message = "Patient email is required")
    @Column(unique = true)
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Patient phone number is required")
    @Column(unique = true)
    @Pattern(regexp = "^\\d{10}$", message = "Please provide a valid phone number")
    @Size(min = 10, max = 10, message = "Please provide a valid phone number")
    private String phoneNumber;

    @NotBlank(message = "Age is mandatory")
    @Pattern(regexp = "^\\d{1,2}$", message = "Age must be between 1 and 2 digits")
    private String age;

    @NotBlank(message = "Patient username is required")
    @Column(unique = true)
    private String username;

    @NotBlank(message = "Password is mandatory")
    private String password;

    @Column(nullable = true) // make it nullable temporarily
    @Enumerated(EnumType.STRING)
    private RoleEnum role = RoleEnum.PATIENT;

    // One-to-many relationship with Appointment
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonManagedReference("appointment-patient")  // Same unique name as in Appointment entity
    private List<Appointment> appointments;

    // Getters and Setters
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

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum roleEnum) {
        this.role = roleEnum;
    }
}
