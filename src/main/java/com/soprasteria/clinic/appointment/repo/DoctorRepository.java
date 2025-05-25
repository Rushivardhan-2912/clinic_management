package com.soprasteria.clinic.appointment.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.soprasteria.clinic.appointment.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

	Optional<Doctor> findByUsername(String username);

	Optional<Doctor> findById(Long id);
}
