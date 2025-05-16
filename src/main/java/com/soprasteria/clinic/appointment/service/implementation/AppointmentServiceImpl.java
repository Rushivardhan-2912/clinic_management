package com.soprasteria.clinic.appointment.service.implementation;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.entity.*;
import com.soprasteria.clinic.appointment.exception.ClinicExceptionHandler.*;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.AppointmentRepository;
import com.soprasteria.clinic.appointment.repo.AvailabilityRepository;
import com.soprasteria.clinic.appointment.repo.DoctorRepository;
import com.soprasteria.clinic.appointment.repo.PatientRepository;
import com.soprasteria.clinic.appointment.service.AppointmentService;
import com.soprasteria.clinic.appointment.util.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.soprasteria.clinic.appointment.util.GenericMessages.*;

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
    public ResponseEntity<?> bookAppointment(AppointmentDTO appointmentDTO, Long patientId, String loggedInUsername) {
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(String.format(PATIENT_NOT_FOUND, patientId)));

            if (!loggedInUsername.equals(patient.getUsername())) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctor().getId())
                    .orElseThrow(() -> new DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND, appointmentDTO.getDoctor().getId())));

            LocalDate date = appointmentDTO.getAppointmentDate();
            LocalTime startTime = appointmentDTO.getAppointmentStartTime();
            LocalTime endTime = appointmentDTO.getAppointmentEndTime();

            if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
                throw new InvalidTimeSlotException(INVALID_TIME_RANGE);
            }

            boolean free = availabilityRepository.isTimeSlotAvailable(doctor.getId(), date, startTime, endTime);
            if (!free) {
                throw new InvalidTimeSlotException(TIME_SLOT_NOT_AVAILABLE);
            }

            boolean alreadyBooked = appointmentRepository.existsBookedAppointment(doctor.getId(), date, startTime, endTime);
            if (alreadyBooked) {
                throw new InvalidTimeSlotException(TIME_SLOT_ALREADY_BOOKED);
            }

            Appointment appointment = globalMapper.toAppointmentEntity(appointmentDTO, doctor, patient);
            appointment.setAppointmentStatus(Status.BOOKED);
            Appointment saved = appointmentRepository.save(appointment);

            handleOverlappingAvailabilities(doctor, date, startTime, endTime);

            return ResponseEntity.status(HttpStatus.CREATED).body(globalMapper.toAppointmentDTO(saved));
        } catch (PatientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (DoctorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (InvalidTimeSlotException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error booking appointment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> viewAllAppointmentsForPatient(Long patientId , Authentication authentication) {
        try {
            String loginUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(String.format(PATIENT_NOT_FOUND, patientId)));

            if (!(patient.getUsername().equals(loginUsername) || isAdmin)) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            List<Appointment> appointments = appointmentRepository.findAppointmentsByPatientId(patientId);
            List<AppointmentDTO> result = appointments.stream()
                    .map(globalMapper::toAppointmentDTO)
                    .toList();

            return ResponseEntity.ok(result);
        } catch (PatientNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error viewing appointments for patient", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> viewAllAppointmentsForDoctor(Long doctorId , Authentication authentication) {
        try {
            String loginUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND, doctorId)));

            if (!(doctor.getUsername().equals(loginUsername) || isAdmin)) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            List<Appointment> appointments = appointmentRepository.findAppointmentsByDoctorId(doctorId);
            List<AppointmentDTO> result = appointments.stream()
                    .map(globalMapper::toAppointmentDTO)
                    .toList();

            return ResponseEntity.ok(result);
        } catch (DoctorNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error viewing appointments for doctor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> viewAllAppointments() {
        try {
            List<AppointmentDTO> result = appointmentRepository.findAll().stream()
                    .map(globalMapper::toAppointmentDTO)
                    .toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error viewing all appointments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> rescheduleAppointment(AppointmentDTO appointmentDTO, Long appointmentId, String loggedInUsername) {
        try {
            Appointment existing = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new IllegalArgumentException(String.format(APPOINTMENT_NOT_FOUND, appointmentId)));

            Patient patient = existing.getPatient();
            if (!patient.getUsername().equals(loggedInUsername)) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            LocalDate newDate = appointmentDTO.getAppointmentDate();
            LocalTime newStart = appointmentDTO.getAppointmentStartTime();
            LocalTime newEnd = appointmentDTO.getAppointmentEndTime();

            if (newStart == null || newEnd == null || !newEnd.isAfter(newStart)) {
                throw new InvalidTimeSlotException(INVALID_TIME_RANGE);
            }

            Doctor doctor = existing.getDoctor();
            boolean available = availabilityRepository.isTimeSlotAvailable(doctor.getId(), newDate, newStart, newEnd);
            boolean alreadyBooked = appointmentRepository.existsBookedAppointment(doctor.getId(), newDate, newStart, newEnd);
            if (!available || alreadyBooked) {
                throw new InvalidTimeSlotException(TIME_SLOT_NOT_AVAILABLE);
            }

            Availability old = new Availability();
            old.setDoctor(doctor);
            old.setAvailabilityDate(existing.getAppointmentDate());
            old.setAvailabilityStartTime(existing.getAppointmentStartTime());
            old.setAvailabilityEndTime(existing.getAppointmentEndTime());
            old.setAvailabilityStatus(Status.AVAILABLE);
            availabilityRepository.save(old);

            existing.setAppointmentDate(newDate);
            existing.setAppointmentStartTime(newStart);
            existing.setAppointmentEndTime(newEnd);
            existing.setAppointmentStatus(Status.RESCHEDULED);
            Appointment updated = appointmentRepository.save(existing);

            handleOverlappingAvailabilities(doctor, newDate, newStart, newEnd);
            return ResponseEntity.ok(globalMapper.toAppointmentDTO(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (InvalidTimeSlotException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error rescheduling appointment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> cancelAppointment(Long appointmentId, Long patientId, Authentication authentication) {
        try {
            String username = authentication.getName();
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new IllegalArgumentException(String.format(APPOINTMENT_NOT_FOUND, appointmentId)));

            if (!appointment.getPatient().getId().equals(patientId) || !appointment.getPatient().getUsername().equals(username)) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            appointment.setAppointmentStatus(Status.CANCELLED);
            appointmentRepository.save(appointment);
            updateAvailabilityForCanceledAppointment(appointment);
            return ResponseEntity.ok("Appointment canceled successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error cancelling appointment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private void updateAvailabilityForCanceledAppointment(Appointment appointment) {
        LocalDate date = appointment.getAppointmentDate();
        LocalTime start = appointment.getAppointmentStartTime();
        LocalTime end = appointment.getAppointmentEndTime();
        Long doctorId = appointment.getDoctor().getId();

        Availability availability = availabilityRepository.findByDoctorAndDateAndTime(doctorId, date, start, end);
        if (availability != null) {
            availability.setAvailabilityStatus(Status.AVAILABLE);
            availabilityRepository.save(availability);
        } else {
            Availability newAvailability = new Availability();
            newAvailability.setDoctor(appointment.getDoctor());
            newAvailability.setAvailabilityDate(date);
            newAvailability.setAvailabilityStartTime(start);
            newAvailability.setAvailabilityEndTime(end);
            newAvailability.setAvailabilityStatus(Status.AVAILABLE);
            availabilityRepository.save(newAvailability);
        }
    }

    private void handleOverlappingAvailabilities(Doctor doctor, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Availability> overlaps = availabilityRepository.findOverlappingAvailabilities(doctor.getId(), date, startTime, endTime);
        for (Availability a : overlaps) {
            if (a.getAvailabilityStartTime().equals(startTime) && a.getAvailabilityEndTime().equals(endTime)) {
                // Exact match — delete directly
                availabilityRepository.delete(a);
                continue;
            }

            // Split before
            if (a.getAvailabilityStartTime().isBefore(startTime)) {
                LocalTime bs = a.getAvailabilityStartTime();
                LocalTime be = startTime;

                if (!availabilityRepository.existsByDoctorAndDateAndTime(doctor.getId(), date, bs, be)) {
                    Availability before = new Availability();
                    before.setDoctor(doctor);
                    before.setAvailabilityDate(date);
                    before.setAvailabilityStartTime(bs);
                    before.setAvailabilityEndTime(be);
                    before.setAvailabilityStatus(Status.AVAILABLE);
                    availabilityRepository.save(before);
                }
            }

            // Split after
            if (a.getAvailabilityEndTime().isAfter(endTime)) {
                LocalTime as = endTime;
                LocalTime ae = a.getAvailabilityEndTime();

                if (!availabilityRepository.existsByDoctorAndDateAndTime(doctor.getId(), date, as, ae)) {
                    Availability after = new Availability();
                    after.setDoctor(doctor);
                    after.setAvailabilityDate(date);
                    after.setAvailabilityStartTime(as);
                    after.setAvailabilityEndTime(ae);
                    after.setAvailabilityStatus(Status.AVAILABLE);
                    availabilityRepository.save(after);
                }
            }
            a.setAvailabilityStatus(Status.BOOKED);
        }
    }

}
