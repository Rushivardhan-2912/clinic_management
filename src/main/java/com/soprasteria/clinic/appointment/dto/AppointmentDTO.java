package com.soprasteria.clinic.appointment.dto;


import com.soprasteria.clinic.appointment.entity.StatusEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentDTO {

	private Long id;

	@NotNull(message = "Appointment date cannot be null")
	@FutureOrPresent(message = "Appointment date must be today or future")
	private LocalDate date;

	@NotNull(message = "Start time cannot be null")
	@Future(message = "Appointment time must be future")
    private LocalTime startTime;

	@NotNull(message = "End time cannot be null")
	@Future(message = "Appointment time must be future")
    private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@NotNull(message = "Status is mandatory")
    private StatusEnum status;
    
    private PatientDTO patient;
    private DoctorDTO doctor;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum appointmentStatusEnum) {
		this.status = appointmentStatusEnum;
	}

	public PatientDTO getPatient() {
		return patient;
	}

	public void setPatient(PatientDTO patient) {
		this.patient = patient;
	}

	public DoctorDTO getDoctor() {
		return doctor;
	}

	public void setDoctor(DoctorDTO doctor) {
		this.doctor = doctor;
	}
}