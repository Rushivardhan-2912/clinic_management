package com.soprasteria.clinic.appointment.repo;

import com.soprasteria.clinic.appointment.entity.StatusEnum;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.soprasteria.clinic.appointment.entity.Availability;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    Page<Availability> findByDoctorId(Long doctorId, Pageable pageable);

    @Query("SELECT a FROM Availability a WHERE a.doctor.id = :doctorId")
    List<Availability> findByDoctorId(@Param("doctorId") Long doctorId);

    // Check if a time slot is available for a doctor
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Availability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.date = :date " +
            "AND a.startTime <= :startTime " +
            "AND a.endTime >= :endTime")
    boolean isTimeSlotAvailable(@Param("doctorId") Long doctorId,
                                @Param("date") LocalDate date,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
            "FROM Availability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.date = :date " +
            "AND a.startTime = :startTime " +
            "AND a.endTime = :endTime")
    boolean existsByDoctorAndDateAndTime(@Param("doctorId") Long doctorId,
                                         @Param("date") LocalDate date,
                                         @Param("startTime") LocalTime startTime,
                                         @Param("endTime") LocalTime endTime);

    // Find overlapping availability slots for a doctor
    @Query("SELECT a FROM Availability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.date = :date " +
            "AND (a.startTime < :endTime " +
            "AND a.endTime > :startTime)")
    List<Availability> findOverlappingAvailabilities(@Param("doctorId") Long doctorId,
                                                     @Param("date") LocalDate date,
                                                     @Param("startTime") LocalTime startTime,
                                                     @Param("endTime") LocalTime endTime);

    @Query("SELECT a FROM Availability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.date = :date " +
            "AND a.id <> :availabilityId " +
            "AND (a.startTime < :endTime " +
            "AND a.endTime > :startTime)")
    List<Availability> findOverlappingAvailabilitiesForUpdate(@Param("doctorId") Long doctorId,
                                                              @Param("availabilityId") Long availabilityId,
                                                              @Param("date") LocalDate date,
                                                              @Param("startTime") LocalTime startTime,
                                                              @Param("endTime") LocalTime endTime);

    // Update availability status for a specific time slot
    @Modifying
    @Query("UPDATE Availability a SET a.status = :status, a.date = :date " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.startTime = :startTime " +
            "AND a.endTime = :endTime")
    int updateAvailability(@Param("doctorId") Long doctorId,
                           @Param("startTime") LocalTime startTime,
                           @Param("endTime") LocalTime endTime,
                           @Param("date") LocalDate date,
                           @Param("status") StatusEnum status);

    // Find availability by doctor, date, and exact time slot
    @Query("SELECT a FROM Availability a " +
            "WHERE a.doctor.id = :doctorId " +
            "AND a.date = :appointmentDate " +
            "AND a.startTime = :startTime " +
            "AND a.endTime = :endTime")
    Availability findByDoctorAndDateAndTime(@Param("doctorId") Long doctorId,
                                            @Param("appointmentDate") LocalDate appointmentDate,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime);


    @Modifying
    @Query("UPDATE Availability a SET a.status = 'EXPIRED' " +
            "WHERE (a.date < :today) " +
            "OR (a.date = :today AND a.endTime < :nowTime) " +
            "AND a.status = 'AVAILABLE'")
    int markPastAsExpired(@Param("today") LocalDate today,
                          @Param("nowTime") LocalTime nowTime);

}
