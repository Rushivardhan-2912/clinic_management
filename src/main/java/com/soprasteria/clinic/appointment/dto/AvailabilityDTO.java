package com.soprasteria.clinic.appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityDTO {

	private Long availabilityId;
	private LocalDate availabilityDate;
	private LocalTime availabilityStartTime;
	private LocalTime availabilityEndTime;
	private String availabilityStatus;
	private DoctorDTO doctor;

	public AvailabilityDTO() {
	}

	public Long getAvailabilityId() {
		return availabilityId;
	}

	public void setAvailabilityId(Long availabilityId) {
		this.availabilityId = availabilityId;
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

	public String getAvailabilityStatus() {
		return availabilityStatus;
	}

	public void setAvailabilityStatus(String availabilityStatus) {
		this.availabilityStatus = availabilityStatus;
	}

	public DoctorDTO getDoctor() {
		return doctor;
	}

	public void setDoctor(DoctorDTO doctor) {
		this.doctor = doctor;
	}
}
