package com.soprasteria.clinic.appointment.service.implementation;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.entity.Appointment;
import com.soprasteria.clinic.appointment.entity.Availability;
import com.soprasteria.clinic.appointment.entity.Doctor;
import com.soprasteria.clinic.appointment.entity.Patient;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.*;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AppointmentRepository;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.AppointmentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger logger = LogManager.getLogger(AppointmentServiceImpl.class);

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private GlobalMapper globalMapper;

    @Override
    @Transactional
    public ResponseEntity<?> bookAppointment(AppointmentDTO appointmentDTO, Long patientId, Authentication authentication) {
        try {
            String loggedInUsername = authentication.getName();
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException("Patient with ID '" + patientId + "' not found"));

            if (!loggedInUsername.equals(patient.getUsername())) {
                throw new UnauthorizedAccessException("You are not authorized to book an appointment for another patient.");
            }

            Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctor().getDoctor_id())
                    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + appointmentDTO.getDoctor().getDoctor_id()));

            LocalDate date = appointmentDTO.getAppointment_date();
            LocalTime startTime = appointmentDTO.getAppointment_startTime();
            LocalTime endTime = appointmentDTO.getAppointment_endTime();

            if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
                throw new InvalidTimeSlotException("Invalid appointment time range.");
            }

            boolean free = availabilityRepository.isTimeSlotAvailable(doctor.getDoctor_id(), date, startTime, endTime);
            if (!free) {
                throw new InvalidTimeSlotException("The selected time slot is not available.");
            }

            boolean alreadyBooked = appointmentRepository.existsBookedAppointment(doctor.getDoctor_id(), date, startTime, endTime);
            if (alreadyBooked) {
                throw new InvalidTimeSlotException("Appointment already booked for the same slot.");
            }

            Appointment appointment = globalMapper.toAppointmentEntity(appointmentDTO, doctor, patient);
            appointment.setAppointment_status("Appointment_Booked");
            Appointment saved = appointmentRepository.save(appointment);

            handleOverlappingAvailabilities(doctor, date, startTime, endTime);
            return ResponseEntity.status(HttpStatus.CREATED).body(globalMapper.toAppointmentDTO(saved));
        } catch (Exception e) {
            logger.error("Error booking appointment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> viewAllAppointmentsForPatient(Long patientId, int page, int size, Authentication authentication) {
        try {
            String loginUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException("Patient not found with ID: " + patientId));

            if (!(patient.getUsername().equals(loginUsername) || isAdmin)) {
                throw new UnauthorizedAccessException("You are not authorized to view these appointments.");
            }

            Page<Appointment> pageData = appointmentRepository.findAppointmentsByPatientId(patientId, PageRequest.of(page, size));
            List<AppointmentDTO> result = pageData.map(globalMapper::toAppointmentDTO).getContent();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error viewing appointments for patient", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> viewAllAppointmentsForDoctor(Long doctorId, int page, int size, Authentication authentication) {
        try {
            String loginUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new DoctorNotFoundException("Doctor not found with ID: " + doctorId));

            if (!(doctor.getUsername().equals(loginUsername) || isAdmin)) {
                throw new UnauthorizedAccessException("You are not authorized to view these appointments.");
            }

            Page<Appointment> pageData = appointmentRepository.findAppointmentsByDoctorId(doctorId, PageRequest.of(page, size));
            List<AppointmentDTO> result = pageData.map(globalMapper::toAppointmentDTO).getContent();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error viewing appointments for doctor", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> viewAllAppointments(int page, int size) {
        try {
            Page<Appointment> pageData = appointmentRepository.findAll(PageRequest.of(page, size));
            List<AppointmentDTO> result = pageData.map(globalMapper::toAppointmentDTO).getContent();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error viewing all appointments", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> rescheduleAppointment(AppointmentDTO appointmentDTO, Long appointmentId, String loggedInUsername) {
        try {
            Appointment existing = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

            Patient patient = existing.getPatient();
            if (!patient.getUsername().equals(loggedInUsername)) {
                throw new UnauthorizedAccessException("You are not authorized to reschedule this appointment.");
            }

            LocalDate newDate = appointmentDTO.getAppointment_date();
            LocalTime newStart = appointmentDTO.getAppointment_startTime();
            LocalTime newEnd = appointmentDTO.getAppointment_endTime();

            if (newStart == null || newEnd == null || !newEnd.isAfter(newStart)) {
                throw new InvalidTimeSlotException("Invalid reschedule time range.");
            }

            Doctor doctor = existing.getDoctor();
            boolean available = availabilityRepository.isTimeSlotAvailable(doctor.getDoctor_id(), newDate, newStart, newEnd);
            boolean alreadyBooked = appointmentRepository.existsBookedAppointment(doctor.getDoctor_id(), newDate, newStart, newEnd);
            if (!available || alreadyBooked) {
                throw new InvalidTimeSlotException("The selected reschedule time slot is not available.");
            }

            Availability old = new Availability();
            old.setDoctor(doctor);
            old.setAvailability_date(existing.getAppointment_date());
            old.setAvailability_startTime(existing.getAppointment_startTime());
            old.setAvailability_endTime(existing.getAppointment_endTime());
            old.setAvailability_status("Available");
            availabilityRepository.save(old);

            existing.setAppointment_date(newDate);
            existing.setAppointment_startTime(newStart);
            existing.setAppointment_endTime(newEnd);
            existing.setAppointment_status("Appointment_Rescheduled");
            Appointment updated = appointmentRepository.save(existing);

            handleOverlappingAvailabilities(doctor, newDate, newStart, newEnd);
            return ResponseEntity.ok(globalMapper.toAppointmentDTO(updated));
        } catch (Exception e) {
            logger.error("Error rescheduling appointment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> cancelAppointment(Long appointmentId, Long patientId, Authentication authentication) {
        try {
            String username = authentication.getName();
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

            if (!appointment.getPatient().getPatient_id().equals(patientId)) {
                throw new UnauthorizedAccessException("You are not authorized to cancel this appointment.");
            }

            if (!appointment.getPatient().getUsername().equals(username)) {
                throw new UnauthorizedAccessException("You can only cancel your own appointments.");
            }

            appointment.setAppointment_status("Canceled");
            appointmentRepository.save(appointment);
            updateAvailabilityForCanceledAppointment(appointment);
            return ResponseEntity.ok("Appointment canceled successfully.");
        } catch (Exception e) {
            logger.error("Error cancelling appointment", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private void updateAvailabilityForCanceledAppointment(Appointment appointment) {
        LocalDate date = appointment.getAppointment_date();
        LocalTime start = appointment.getAppointment_startTime();
        LocalTime end = appointment.getAppointment_endTime();
        Long doctorId = appointment.getDoctor().getDoctor_id();

        Availability availability = availabilityRepository.findByDoctorAndDateAndTime(doctorId, date, start, end);
        if (availability != null) {
            availability.setAvailability_status("Available");
            availabilityRepository.save(availability);
        } else {
            Availability newAvailability = new Availability();
            newAvailability.setDoctor(appointment.getDoctor());
            newAvailability.setAvailability_date(date);
            newAvailability.setAvailability_startTime(start);
            newAvailability.setAvailability_endTime(end);
            newAvailability.setAvailability_status("Available");
            availabilityRepository.save(newAvailability);
        }
    }

    private void handleOverlappingAvailabilities(Doctor doctor, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Availability> overlaps = availabilityRepository.findOverlappingAvailabilities(doctor.getDoctor_id(), date, startTime, endTime);
        for (Availability a : overlaps) {
            // Exact match: delete directly
            if (a.getAvailability_startTime().equals(startTime) && a.getAvailability_endTime().equals(endTime)) {
                availabilityRepository.delete(a);
                continue;
            }

            boolean hasSplit = false;

            if (a.getAvailability_startTime().isBefore(startTime)) {
                LocalTime bs = a.getAvailability_startTime();
                LocalTime be = startTime;

                if (!availabilityRepository.existsByDoctorAndDateAndTime(doctor.getDoctor_id(), date, bs, be)) {
                    Availability before = new Availability();
                    before.setDoctor(doctor);
                    before.setAvailability_date(date);
                    before.setAvailability_startTime(bs);
                    before.setAvailability_endTime(be);
                    before.setAvailability_status("Available");
                    availabilityRepository.save(before);
                    hasSplit = true;
                }
            }

            if (a.getAvailability_endTime().isAfter(endTime)) {
                LocalTime as = endTime;
                LocalTime ae = a.getAvailability_endTime();

                if (!availabilityRepository.existsByDoctorAndDateAndTime(doctor.getDoctor_id(), date, as, ae)) {
                    Availability after = new Availability();
                    after.setDoctor(doctor);
                    after.setAvailability_date(date);
                    after.setAvailability_startTime(as);
                    after.setAvailability_endTime(ae);
                    after.setAvailability_status("Available");
                    availabilityRepository.save(after);
                    hasSplit = true;
                }
            }

            if (hasSplit) {
                availabilityRepository.delete(a);
            }
        }
    }
}
