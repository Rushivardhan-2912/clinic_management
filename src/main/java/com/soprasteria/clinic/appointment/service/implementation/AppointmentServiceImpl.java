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
import com.soprasteria.clinic.appointment.entity.StatusEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.soprasteria.clinic.appointment.util.GenericMessages.*;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger logger = LogManager.getLogger(AppointmentServiceImpl.class);

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AvailabilityRepository availabilityRepository;
    private final GlobalMapper globalMapper;

    public AppointmentServiceImpl(DoctorRepository doctorRepository,
                                PatientRepository patientRepository,
                                AppointmentRepository appointmentRepository,
                                AvailabilityRepository availabilityRepository,
                                GlobalMapper globalMapper) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.globalMapper = globalMapper;
    }

    @Override
    @Transactional
    public ResponseEntity<?> bookAppointment(AppointmentDTO appointmentDTO, Long patientId, String loggedInUsername) {
        try {
            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(String.format(PATIENT_NOT_FOUND, patientId)));

            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!(isAdmin || loggedInUsername.equals(patient.getUsername()))) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            Doctor doctor = doctorRepository.findById(appointmentDTO.getDoctor().getId())
                    .orElseThrow(() -> new DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND, appointmentDTO.getDoctor().getId())));


            logger.info("Attempting to book appointment for patient with id: {}",patientId);
            LocalDate date = appointmentDTO.getDate();
            LocalTime startTime = appointmentDTO.getStartTime();
            LocalTime endTime = appointmentDTO.getEndTime();

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
            appointment.setStatus(StatusEnum.BOOKED);
            Appointment saved = appointmentRepository.save(appointment);

            logger.info("Succcessfully booked the appointment with booking id:{} ",appointment.getId());

            handleOverlappingAvailabilities(doctor, date, startTime, endTime);

            logger.info("Succesfully handled overlapping times");
            return ResponseEntity.status(HttpStatus.CREATED).body(globalMapper.toAppointmentDTO(saved));
        } catch (PatientNotFoundException e) {
            logger.error("Error booking appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (DoctorNotFoundException e) {
            logger.error("Error booking appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            logger.error("Error booking appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (InvalidTimeSlotException e) {
            logger.error("Error booking appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error booking appointment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> viewAllAppointmentsForPatient(Long patientId, String loginUsername, int page, int size) {
        logger.info("Fetching appointments for patient ID: {}, requested by user: {}", patientId, loginUsername);

        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            Patient patient = patientRepository.findById(patientId)
                    .orElseThrow(() -> new PatientNotFoundException(String.format(PATIENT_NOT_FOUND, patientId)));

            if (!(patient.getUsername().equals(loginUsername) || isAdmin)) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<AppointmentDTO> appointmentPage = appointmentRepository.findAppointmentsByPatientId(patientId, pageable)
                    .map(globalMapper::toAppointmentDTO);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("data", appointmentPage.getContent());
            response.put("pageNo", appointmentPage.getNumber());
            response.put("pageSize", appointmentPage.getSize());
            response.put("totalResults", appointmentPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (PatientNotFoundException e) {
            logger.error("Error viewing appointments for patient", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            logger.error("Error viewing appointments for patient", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error viewing appointments for patient", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> viewAllAppointmentsForDoctor(Long doctorId, String loginUsername, int page, int size) {
        logger.info("Fetching appointments for doctor ID: {}, requested by user: {}", doctorId, loginUsername);

        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new DoctorNotFoundException(String.format(DOCTOR_NOT_FOUND, doctorId)));

            if (!(doctor.getUsername().equals(loginUsername) || isAdmin)) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<AppointmentDTO> appointmentPage = appointmentRepository.findAppointmentsByDoctorId(doctorId, pageable)
                    .map(globalMapper::toAppointmentDTO);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("data", appointmentPage.getContent());
            response.put("pageNo", appointmentPage.getNumber());
            response.put("pageSize", appointmentPage.getSize());
            response.put("totalResults", appointmentPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (DoctorNotFoundException e) {
            logger.error("Error viewing appointments for patient", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            logger.error("Error viewing appointments for patient", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error viewing appointments for doctor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> viewAllAppointments(int page, int size) {
        logger.info("Fetching all appointments, page: {}, size: {}", page, size);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<AppointmentDTO> appointmentPage = appointmentRepository.findAll(pageable)
                    .map(globalMapper::toAppointmentDTO);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("data", appointmentPage.getContent());
            response.put("pageNo", appointmentPage.getNumber());
            response.put("pageSize", appointmentPage.getSize());
            response.put("totalResults", appointmentPage.getTotalElements());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error viewing all appointments", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> rescheduleAppointment(AppointmentDTO appointmentDTO, Long appointmentId, String loggedInUsername) {
        logger.info("Rescheduling appointment ID: {} by user: {}", appointmentId, loggedInUsername);
        try {
            Appointment existing = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new AppointmentNotFoundException(String.format(APPOINTMENT_NOT_FOUND, appointmentId)));

            Patient patient = existing.getPatient();
            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!(isAdmin || patient.getUsername().equals(loggedInUsername))) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            LocalDate newDate = appointmentDTO.getDate();
            LocalTime newStart = appointmentDTO.getStartTime();
            LocalTime newEnd = appointmentDTO.getEndTime();

            if (newStart == null || newEnd == null || !newEnd.isAfter(newStart)) {
                throw new InvalidTimeSlotException(INVALID_TIME_RANGE);
            }

            Doctor doctor = existing.getDoctor();

            // Step 1: Check overlapping availability (not just containment)
            List<Availability> overlappingAvailabilities = availabilityRepository.findOverlappingAvailabilities(
                    doctor.getId(), newDate, newStart, newEnd
            );
            boolean available = !overlappingAvailabilities.isEmpty();

            // Step 2: Ensure the new time slot is not already booked
            boolean alreadyBooked = appointmentRepository.existsBookedAppointment(doctor.getId(), newDate, newStart, newEnd);

            if (!available || alreadyBooked) {
                throw new InvalidTimeSlotException(TIME_SLOT_NOT_AVAILABLE);
            }

            // Step 3: Restore the previous time slot as available
            Availability old = new Availability();
            old.setDoctor(doctor);
            old.setDate(existing.getDate());
            old.setStartTime(existing.getStartTime());
            old.setEndTime(existing.getEndTime());
            old.setStatus(StatusEnum.AVAILABLE);
            availabilityRepository.save(old);

            // Step 4: Update appointment details
            existing.setDate(newDate);
            existing.setStartTime(newStart);
            existing.setEndTime(newEnd);
            existing.setStatus(StatusEnum.RESCHEDULED);
            Appointment updated = appointmentRepository.save(existing);
            logger.info("Successfully rescheduled appointment ID: {} to date: {}, start: {}, end: {}",
                    appointmentId, newDate, newStart, newEnd);

            // Step 5: Handle overlapping availability updates
            handleOverlappingAvailabilities(doctor, newDate, newStart, newEnd);

            return ResponseEntity.ok(globalMapper.toAppointmentDTO(updated));
        } catch (AppointmentNotFoundException e) {
            logger.error("Error rescheduling appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (InvalidTimeSlotException e) {
            logger.error("Error rescheduling appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            logger.error("Error rescheduling appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error rescheduling appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @Override
    @Transactional
    public ResponseEntity<?> cancelAppointment(Long appointmentId, Long patientId, String username) {
        logger.info("Cancelling appointment ID: {} by user: {}", appointmentId, username);

        try {
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new AppointmentNotFoundException(String.format(APPOINTMENT_NOT_FOUND, appointmentId)));

            var auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!(isAdmin || appointment.getPatient().getUsername().equals(username))) {
                throw new UnauthorizedAccessException(UNAUTHORIZED);
            }

            appointment.setStatus(StatusEnum.CANCELLED);
            appointmentRepository.save(appointment);
            updateAvailabilityForCanceledAppointment(appointment);

            logger.info("Successfully cancelled appointment ID: {}", appointmentId);
            return ResponseEntity.ok("Appointment canceled successfully.");
        } catch (AppointmentNotFoundException e) {
            logger.error("Error cancelling appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (UnauthorizedAccessException e) {
            logger.error("Error cancelling appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error cancelling appointment", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private void updateAvailabilityForCanceledAppointment(Appointment appointment) {
        LocalDate date = appointment.getDate();
        LocalTime start = appointment.getStartTime();
        LocalTime end = appointment.getEndTime();
        Long doctorId = appointment.getDoctor().getId();

        Availability availability = availabilityRepository.findByDoctorAndDateAndTime(doctorId, date, start, end);
        if (availability != null) {
            availability.setStatus(StatusEnum.AVAILABLE);
            availabilityRepository.save(availability);
        } else {
            Availability newAvailability = new Availability();
            newAvailability.setDoctor(appointment.getDoctor());
            newAvailability.setDate(date);
            newAvailability.setStartTime(start);
            newAvailability.setEndTime(end);
            newAvailability.setStatus(StatusEnum.AVAILABLE);
            availabilityRepository.save(newAvailability);
        }
    }

    public void handleOverlappingAvailabilities(Doctor doctor, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Availability> overlaps = availabilityRepository.findOverlappingAvailabilities(doctor.getId(), date, startTime, endTime);
        logger.info("Handling overlapping availabilities for doctor ID: {}, date: {}, time: {} - {}",
                            doctor.getId(), date, startTime, endTime);
        for (Availability a : overlaps) {
            if (a.getStartTime().equals(startTime) && a.getEndTime().equals(endTime)) {
                // Exact match — delete directly
                availabilityRepository.delete(a);
                continue;
            }

            // Split before
            if (a.getStartTime().isBefore(startTime)) {
                LocalTime bs = a.getStartTime();
                LocalTime be = startTime;

                if (!availabilityRepository.existsByDoctorAndDateAndTime(doctor.getId(), date, bs, be)) {
                    Availability before = new Availability();
                    before.setDoctor(doctor);
                    before.setDate(date);
                    before.setStartTime(bs);
                    before.setEndTime(be);
                    before.setStatus(StatusEnum.AVAILABLE);
                    availabilityRepository.save(before);
                }
            }

            // Split after
            if (a.getEndTime().isAfter(endTime)) {
                LocalTime as = endTime;
                LocalTime ae = a.getEndTime();

                if (!availabilityRepository.existsByDoctorAndDateAndTime(doctor.getId(), date, as, ae)) {
                    Availability after = new Availability();
                    after.setDoctor(doctor);
                    after.setDate(date);
                    after.setStartTime(as);
                    after.setEndTime(ae);
                    after.setStatus(StatusEnum.AVAILABLE);
                    availabilityRepository.save(after);
                }
            }
            a.setStartTime(startTime);
            a.setEndTime(endTime);
            a.setStatus(StatusEnum.BOOKED);
        }
    }

}
