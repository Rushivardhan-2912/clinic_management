package com.soprasteria.clinic.appointment.repo;

import com.soprasteria.clinic.appointment.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

	Optional<Doctor> findByUsername(String username);

	Optional<Doctor> findById(Long doctor_id);
}
