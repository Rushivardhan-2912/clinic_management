package com.soprasteria.clinic.appointment.repo;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.soprasteria.clinic.appointment.entity.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	Page<Appointment> findAppointmentsByPatientId(Long patientId, Pageable pageable);
	Page<Appointment> findAppointmentsByDoctorId(Long doctorId, Pageable pageable);

	@Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId")
	List<Appointment> findAppointmentsByPatientId(@Param("patientId") Long patientId);

	@Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId")
	List<Appointment> findAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

	@Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
			"a.doctor.id = :doctorId AND " +
			"a.date = :date AND " +
			"(a.startTime < :endTime AND a.endTime > :startTime) AND " +
			"a.status = StatusEnum.BOOKED")
	boolean existsBookedAppointment(@Param("doctorId") Long doctorId,
									@Param("date") LocalDate date,
									@Param("startTime") LocalTime startTime,
									@Param("endTime") LocalTime endTime);

	@Modifying
	@Query("DELETE FROM Appointment a WHERE a.date < :today")
	void deletePastData(@Param("today") LocalDate today);
}

