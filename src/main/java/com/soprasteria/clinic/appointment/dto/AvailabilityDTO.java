package com.soprasteria.clinic.appointment.dto;

import com.soprasteria.clinic.appointment.entity.Status;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityDTO {

	private Long id;
	private LocalDate availabilityDate;
	private LocalTime availabilityStartTime;
	private LocalTime availabilityEndTime;
	private Status availabilityStatus;
	private DoctorDTO doctor;

	public AvailabilityDTO() {
	}

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

	public void setAvailabilityStatus(Status availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
	}

	public DoctorDTO getDoctor() {
		return doctor;
	}

	public void setDoctor(DoctorDTO doctor) {
		this.doctor = doctor;
	}
}
