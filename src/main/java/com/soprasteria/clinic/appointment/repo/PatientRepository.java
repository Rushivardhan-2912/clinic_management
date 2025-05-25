package com.soprasteria.clinic.appointment.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.soprasteria.clinic.appointment.entity.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

	Optional<Patient> findByUsername(String username);

	@Query("SELECT p FROM Patient p WHERE p.phoneNumber = :phoneNumber")
	Optional<Patient> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

	@Query("SELECT p FROM Patient p WHERE p.email = :email")
	Optional<Patient> findByEmail(@Param("email") String email);
}
