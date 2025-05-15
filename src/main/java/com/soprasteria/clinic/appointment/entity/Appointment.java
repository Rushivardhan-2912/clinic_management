package com.soprasteria.clinic.appointment.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.soprasteria.clinic.appointment.util.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appointment_seq")
    @SequenceGenerator(name = "appointment_seq", sequenceName = "appointment_sequence", allocationSize = 1)
    private Long id;

    @NotNull(message = "Appointment date cannot be null")
    @FutureOrPresent(message = "Appointment date must be today or future")
    private LocalDate appointmentDate;

    @NotNull(message = "Start time cannot be null")
    private LocalTime appointmentStartTime;

    @NotNull(message = "End time cannot be null")
    private LocalTime appointmentEndTime;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is mandatory")
    private Status appointmentStatus;

    // Many-to-one relationship with Patient
    @ManyToOne
    @JoinColumn(name = "patient_id")
    @JsonBackReference("appointment-patient")  // Unique name for patient reference
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @JsonBackReference("appointment-doctor")  // Unique name for doctor reference
    private Doctor doctor;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentStartTime() {
        return appointmentStartTime;
    }

    public void setAppointmentStartTime(LocalTime appointmentStartTime) {
        this.appointmentStartTime = appointmentStartTime;
    }

    public LocalTime getAppointmentEndTime() {
        return appointmentEndTime;
    }

    public void setAppointmentEndTime(LocalTime appointmentEndTime) {
        this.appointmentEndTime = appointmentEndTime;
    }

    public Status getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(Status appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}
