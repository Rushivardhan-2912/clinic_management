package com.soprasteria.clinic.appointment.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appointment_seq")
    @SequenceGenerator(name = "appointment_seq", sequenceName = "appointment_sequence", allocationSize = 1)
    private Long appointment_id;

    private LocalDate appointment_date;
    private LocalTime appointment_startTime;
    private LocalTime appointment_endTime;
    private String appointment_status;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    // Getters and Setters
    public Long getAppointment_id() {
        return appointment_id;
    }

    public void setAppointment_id(Long appointment_id) {
        this.appointment_id = appointment_id;
    }

    public LocalDate getAppointment_date() {
        return appointment_date;
    }

    public void setAppointment_date(LocalDate appointment_date) {
        this.appointment_date = appointment_date;
    }

    public LocalTime getAppointment_startTime() {
        return appointment_startTime;
    }

    public void setAppointment_startTime(LocalTime appointment_startTime) {
        this.appointment_startTime = appointment_startTime;
    }

    public LocalTime getAppointment_endTime() {
        return appointment_endTime;
    }

    public void setAppointment_endTime(LocalTime appointment_endTime) {
        this.appointment_endTime = appointment_endTime;
    }

    public String getAppointment_status() {
        return appointment_status;
    }

    public void setAppointment_status(String appointment_status) {
        this.appointment_status = appointment_status;
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
