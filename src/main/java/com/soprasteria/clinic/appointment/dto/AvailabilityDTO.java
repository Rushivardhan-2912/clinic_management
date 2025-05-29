package com.soprasteria.clinic.appointment.dto;

import com.soprasteria.clinic.appointment.entity.StatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityDTO {

	private Long id;

	@NotNull(message = "Availability date cannot be null")
	@FutureOrPresent(message = "Availability date must be today or future")
	private LocalDate date;


	@NotNull(message = "Availability start time cannot be null")
	private LocalTime startTime;

	@NotNull(message = "Availability end time cannot be null")
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StatusEnum status;

	private DoctorDTO doctor;

	public AvailabilityDTO() {
	}

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

	public void setStatus(StatusEnum availabilityStatusEnum) {
		this.status = availabilityStatusEnum;
	}

	public DoctorDTO getDoctor() {
		return doctor;
	}

	public void setDoctor(DoctorDTO doctor) {
		this.doctor = doctor;
	}
}
