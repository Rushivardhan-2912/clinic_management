package com.soprasteria.clinic.appointment.entity;

import java.util.List;
import jakarta.persistence.*;

@Entity
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_seq")
    @SequenceGenerator(name = "patient_seq", sequenceName = "patient_sequence", allocationSize = 1)
    private Long patient_id;

    private String patient_name;
    private String patient_email;
    private String patient_phoneNumber;
    private String username;
    private String patient_password;
    private String role = "PATIENT";

    // Getters and Setters
    public Long getPatient_id() {
        return patient_id;
    }

    public void setPatient_id(Long patient_id) {
        this.patient_id = patient_id;
    }

    public String getPatient_name() {
        return patient_name;
    }

    public void setPatient_name(String patient_name) {
        this.patient_name = patient_name;
    }

    public String getPatient_email() {
        return patient_email;
    }

    public void setPatient_email(String patient_email) {
        this.patient_email = patient_email;
    }

    public String getPatient_phoneNumber() {
        return patient_phoneNumber;
    }

    public void setPatient_phoneNumber(String patient_phoneNumber) {
        this.patient_phoneNumber = patient_phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPatient_password() {
        return patient_password;
    }

    public void setPatient_password(String patient_password) {
        this.patient_password = patient_password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> patient_appointments;

    public List<Appointment> getPatient_appointments() {
        return patient_appointments;
    }

    public void setPatient_appointments(List<Appointment> patient_appointments) {
        this.patient_appointments = patient_appointments;
    }
}
