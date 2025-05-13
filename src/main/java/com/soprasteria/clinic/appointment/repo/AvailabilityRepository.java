package com.soprasteria.clinic.appointment.repo;

import com.soprasteria.clinic.appointment.entity.Availability;
import com.soprasteria.clinic.appointment.util.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    @Query("SELECT a FROM Availability a WHERE a.doctor.id = :doctorId")
    List<Availability> findByDoctorId(@Param("doctorId") Long doctorId);

    // Check if a time slot is available for a doctor
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Availability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.availabilityDate = :date " +
            "AND a.availabilityStartTime <= :startTime " +
            "AND a.availabilityEndTime >= :endTime")
    boolean isTimeSlotAvailable(@Param("doctorId") Long doctorId,
                                @Param("date") LocalDate date,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Availability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.availabilityDate = :date " +
            "AND a.availabilityStartTime = :startTime " +
            "AND a.availabilityEndTime = :endTime")
    boolean existsByDoctorAndDateAndTime(@Param("doctorId") Long doctorId,
                                         @Param("date") LocalDate date,
                                         @Param("startTime") LocalTime startTime,
                                         @Param("endTime") LocalTime endTime);

    // Find overlapping availability slots for a doctor
    @Query("SELECT a FROM Availability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.availabilityDate = :date " +
            "AND (a.availabilityStartTime < :endTime " +
            "AND a.availabilityEndTime > :startTime)")
    List<Availability> findOverlappingAvailabilities(@Param("doctorId") Long doctorId,
                                                     @Param("date") LocalDate date,
                                                     @Param("startTime") LocalTime startTime,
                                                     @Param("endTime") LocalTime endTime);

    // Update availability status for a specific time slot
    @Modifying
    @Query("UPDATE Availability a SET a.availabilityStatus = :status, a.availabilityDate = :date " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.availabilityStartTime = :startTime " +
            "AND a.availabilityEndTime = :endTime")
    int updateAvailability(@Param("doctorId") Long doctorId,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime,
                           @Param("date") LocalDate date,
                           @Param("status") Status status);

    // Find availability by doctor, date, and exact time slot
    @Query("SELECT a FROM Availability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.availabilityDate = :appointmentDate " +
            "AND a.availabilityStartTime = :startTime " +
            "AND a.availabilityEndTime = :endTime")
    Availability findByDoctorAndDateAndTime(@Param("doctorId") Long doctorId,
                                            @Param("appointmentDate") LocalDate appointmentDate,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime);

    @Modifying
    @Query("DELETE FROM Availability e WHERE e.availabilityDate < :today")
    void deletePastData(@Param("today") LocalDate today);
}
