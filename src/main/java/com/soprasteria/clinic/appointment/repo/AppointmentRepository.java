package com.soprasteria.clinic.appointment.repo;

import com.soprasteria.clinic.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	@Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId")
	List<Appointment> findAppointmentsByPatientId(@Param("patientId") Long patientId);

	@Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId")
	List<Appointment> findAppointmentsByDoctorId(@Param("doctorId") Long doctorId);

	@Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
			"a.doctor.id = :doctorId AND " +
			"a.appointmentDate = :date AND " +
			"(a.appointmentStartTime < :endTime AND a.appointmentEndTime > :startTime) AND " +
			"a.appointmentStatus = Status.BOOKED")
	boolean existsBookedAppointment(@Param("doctorId") Long doctorId,
									@Param("date") LocalDate date,
									@Param("startTime") LocalTime startTime,
									@Param("endTime") LocalTime endTime);

	@Modifying
	@Query("DELETE FROM Appointment a WHERE a.appointmentDate < :today")
	void deletePastData(@Param("today") LocalDate today);
}

