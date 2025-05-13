package com.soprasteria.clinic.appointment.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.soprasteria.clinic.appointment.util.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "availability_seq")
    @SequenceGenerator(name = "availability_seq", sequenceName = "availability_sequence", allocationSize = 1)
    private Long id;

    @NotNull(message = "Availability date cannot be null")
    @FutureOrPresent(message = "Availability date must be today or future")
    private LocalDate availabilityDate;

    @NotNull(message = "Availability start time cannot be null")
    private LocalTime availabilityStartTime;

    @NotNull(message = "Availability end time cannot be null")
    private LocalTime availabilityEndTime;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is mandatory")
    private Status availabilityStatus;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @JsonBackReference
    private Doctor doctor;

    // Default no-argument constructor (this is REQUIRED for JPA)
    public Availability() {
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getAvailabilityDate() {
        return availabilityDate;
    }

    public void setAvailabilityDate(LocalDate availabilityDate) {
        this.availabilityDate = availabilityDate;
    }

    public LocalTime getAvailabilityStartTime() {
        return availabilityStartTime;
    }

    public void setAvailabilityStartTime(LocalTime availabilityStartTime) {
        this.availabilityStartTime = availabilityStartTime;
    }

    public LocalTime getAvailabilityEndTime() {
        return availabilityEndTime;
    }

    public void setAvailabilityEndTime(LocalTime availabilityEndTime) {
        this.availabilityEndTime = availabilityEndTime;
    }

    public Status getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(Status status) {
        this.availabilityStatus = status;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }
}
