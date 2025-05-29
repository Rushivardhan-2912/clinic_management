package com.soprasteria.clinic.appointment.service;

import com.soprasteria.clinic.appointment.dto.AppointmentDTO;
import com.soprasteria.clinic.appointment.dto.DoctorDTO;
import com.soprasteria.clinic.appointment.entity.*;
import com.soprasteria.clinic.appointment.mapper.GlobalMapper;
import com.soprasteria.clinic.appointment.repo.*;
import com.soprasteria.clinic.appointment.service.implementation.AppointmentServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.soprasteria.clinic.appointment.util.GenericMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AppointmentServiceImplTest {

    @InjectMocks
    private AppointmentServiceImpl service;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private GlobalMapper globalMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private final String ADMIN_ROLE = "ROLE_ADMIN";
    private final String PATIENT_ROLE = "ROLE_PATIENT";

    private Patient patient;
    private Doctor doctor;
    private AppointmentDTO appointmentDTO;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup SecurityContextHolder mock
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Common test data
        patient = new Patient();
        patient.setId(1L);
        patient.setUsername("patientUser");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUsername("doctorUser");

        // Mock doctor DTO before using it
        DoctorDTO doctorDTO = new DoctorDTO();
        doctorDTO.setId(doctor.getId());
        doctorDTO.setUsername(doctor.getUsername());
        when(globalMapper.toDoctorDTO(doctor)).thenReturn(doctorDTO);

        appointmentDTO = new AppointmentDTO();
        appointmentDTO.setDoctor(doctorDTO); // Use mocked return value
        appointmentDTO.setDate(LocalDate.now().plusDays(1));
        appointmentDTO.setStartTime(LocalTime.of(10, 0));
        appointmentDTO.setEndTime(LocalTime.of(11, 0));

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setDate(appointmentDTO.getDate());
        appointment.setStartTime(appointmentDTO.getStartTime());
        appointment.setEndTime(appointmentDTO.getEndTime());
        appointment.setStatus(StatusEnum.BOOKED);
    }

    private void mockIsAdmin(boolean isAdmin) {
        if (isAdmin) {
            doReturn(List.of(new SimpleGrantedAuthority(ADMIN_ROLE))).when(authentication).getAuthorities();
        } else {
            doReturn(List.of(new SimpleGrantedAuthority(PATIENT_ROLE))).when(authentication).getAuthorities();
        }
    }


    // 1. Book appointment successfully as patient
    @Test
    void bookAppointment_Success_AsPatient() {
        mockIsAdmin(false);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(availabilityRepository.isTimeSlotAvailable(anyLong(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.existsBookedAppointment(anyLong(), any(), any(), any())).thenReturn(false);
        when(globalMapper.toAppointmentEntity(any(), any(), any())).thenReturn(appointment);
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(globalMapper.toAppointmentDTO(any())).thenReturn(appointmentDTO);

        ResponseEntity<?> response = service.bookAppointment(appointmentDTO, patient.getId(), patient.getUsername());

        assertEquals(201, response.getStatusCodeValue());
        verify(appointmentRepository).save(any());
    }

    // 2. Book appointment successfully as admin
    @Test
    void bookAppointment_Success_AsAdmin() {
        mockIsAdmin(true);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(availabilityRepository.isTimeSlotAvailable(anyLong(), any(), any(), any())).thenReturn(true);
        when(appointmentRepository.existsBookedAppointment(anyLong(), any(), any(), any())).thenReturn(false);
        when(globalMapper.toAppointmentEntity(any(), any(), any())).thenReturn(appointment);
        when(appointmentRepository.save(any())).thenReturn(appointment);
        when(globalMapper.toAppointmentDTO(any())).thenReturn(appointmentDTO);

        ResponseEntity<?> response = service.bookAppointment(appointmentDTO, patient.getId(), "adminUser");

        assertEquals(201, response.getStatusCodeValue());
        verify(appointmentRepository).save(any());
    }

    // 4. Book appointment invalid time range (endTime before startTime)
    @Test
    void bookAppointment_InvalidTimeRange() {
        mockIsAdmin(true);
        appointmentDTO.setStartTime(LocalTime.of(11, 0));
        appointmentDTO.setEndTime(LocalTime.of(10, 0));

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));

        ResponseEntity<?> response = service.bookAppointment(appointmentDTO, patient.getId(), "adminUser");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals(INVALID_TIME_RANGE, response.getBody());
    }

    // 6. Book appointment doctor not found
    @Test
    void bookAppointment_DoctorNotFound() {
        mockIsAdmin(true);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.bookAppointment(appointmentDTO, patient.getId(), "adminUser");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains(String.format(DOCTOR_NOT_FOUND, doctor.getId())));
    }

    // 7. Book appointment time slot not available
    @Test
    void bookAppointment_TimeSlotNotAvailable() {
        mockIsAdmin(true);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(availabilityRepository.isTimeSlotAvailable(anyLong(), any(), any(), any())).thenReturn(false);

        ResponseEntity<?> response = service.bookAppointment(appointmentDTO, patient.getId(), "adminUser");

        assertEquals(400, response.getStatusCodeValue());
        assertEquals(TIME_SLOT_NOT_AVAILABLE, response.getBody());
    }

    // 8. Book appointment time slot already booked
    @Test
    void bookAppointment_TimeSlotAlreadyBooked() {
        mockIsAdmin(true);

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(availabilityRepository.isTimeSlotAvailable(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class))).thenReturn(true);
        when(appointmentRepository.existsBookedAppointment(anyLong(), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class))).thenReturn(true);

        ResponseEntity<?> response = service.bookAppointment(appointmentDTO, patient.getId(), "adminUser");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCodeValue());
        assertEquals(TIME_SLOT_ALREADY_BOOKED, response.getBody());
    }

    // 3. Book appointment unauthorized
    @Test
    void bookAppointment_Unauthorized() {
        mockIsAdmin(false);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));

        ResponseEntity<?> response = service.bookAppointment(appointmentDTO, patient.getId(), "otherUser");

        assertEquals(401, response.getStatusCodeValue());
        assertEquals(UNAUTHORIZED, response.getBody());
    }

    // 5. Book appointment patient not found
    @Test
    void bookAppointment_PatientNotFound() {
        mockIsAdmin(true);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResponseEntity<?> response = service.bookAppointment(appointmentDTO, 999L, "adminUser");

        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains(String.format(PATIENT_NOT_FOUND, 999L)));
    }

    // 9. View all appointments for patient success as patient
    @Test
    void viewAllAppointmentsForPatient_Success_AsPatient() {
        mockIsAdmin(false);

        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(appointmentRepository.findAppointmentsByPatientId(anyLong(), any()))
                .thenReturn(new PageImpl<>(List.of(appointment)));

        when(globalMapper.toAppointmentDTO(any())).thenReturn(appointmentDTO);

        ResponseEntity<?> response = service.viewAllAppointmentsForPatient(patient.getId(), patient.getUsername(), 0, 10);

        assertEquals(200, response.getStatusCodeValue());
        var body = response.getBody();
        assertTrue(body instanceof Map);
        Map<?, ?> map = (Map<?, ?>) body;
        assertEquals(1, ((List<?>) map.get("data")).size());
    }

    // 11. View all appointments for doctor success as doctor
    @Test
    void viewAllAppointmentsForDoctor_Success_AsDoctor() {
        mockIsAdmin(false);

        when(doctorRepository.findById(anyLong())).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findAppointmentsByDoctorId(anyLong(), any()))
                .thenReturn(new PageImpl<>(List.of(appointment)));
        when(globalMapper.toAppointmentDTO(any())).thenReturn(appointmentDTO);

        ResponseEntity<?> response = service.viewAllAppointmentsForDoctor(doctor.getId(), doctor.getUsername(), 0, 10);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> map = (Map<?, ?>) response.getBody();
        assertEquals(1, ((List<?>) map.get("data")).size());
    }

    // 13. Cancel appointment success as patient
    @Test
    void cancelAppointment_Success_AsPatient() {
        mockIsAdmin(false);
        Appointment toCancel = new Appointment();
        toCancel.setId(1L);
        toCancel.setPatient(patient);
        toCancel.setDoctor(doctor);
        toCancel.setDate(LocalDate.now());
        toCancel.setStartTime(LocalTime.of(10, 0));
        toCancel.setEndTime(LocalTime.of(11, 0));
        toCancel.setStatus(StatusEnum.BOOKED);

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(toCancel));
        when(availabilityRepository.findByDoctorAndDateAndTime(anyLong(), any(), any(), any())).thenReturn(null);
        when(availabilityRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = service.cancelAppointment(1L, patient.getId(), patient.getUsername());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Appointment canceled successfully.", response.getBody());
        verify(appointmentRepository).save(any());
        verify(availabilityRepository).save(any());
    }

    // 15. View all appointments success
    @Test
    void viewAllAppointments_Success() {
        when(appointmentRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(appointment)));
        when(globalMapper.toAppointmentDTO(any())).thenReturn(appointmentDTO);

        ResponseEntity<?> response = service.viewAllAppointments(0, 10);

        assertEquals(200, response.getStatusCodeValue());
        Map<?, ?> map = (Map<?, ?>) response.getBody();
        assertEquals(1, ((List<?>) map.get("data")).size());
    }

    @Test
    void cancelAppointment_Success() {
        mockIsAdmin(false);

        Appointment existing = new Appointment();
        existing.setId(1L);
        existing.setPatient(patient);
        existing.setDoctor(doctor);
        existing.setStatus(StatusEnum.BOOKED);

        when(appointmentRepository.findById(anyLong())).thenReturn(Optional.of(existing));
        when(appointmentRepository.save(any())).thenReturn(existing);

        ResponseEntity<?> response = service.cancelAppointment(1L, patient.getId(), patient.getUsername());

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertEquals("Appointment canceled successfully.", response.getBody());
    }

    @Test
    void viewAllAppointmentsForPatient_Success() {
        mockIsAdmin(false);

        List<Appointment> appointments = List.of(appointment);
        Page<Appointment> appointmentPage = new PageImpl<>(appointments);
        when(patientRepository.findById(anyLong())).thenReturn(Optional.of(patient));
        when(appointmentRepository.findAppointmentsByPatientId(anyLong(), any(Pageable.class))).thenReturn(appointmentPage);
        when(globalMapper.toAppointmentDTO(any())).thenReturn(appointmentDTO);

        ResponseEntity<?> response = service.viewAllAppointmentsForPatient(patient.getId(), patient.getUsername(), 0, 10);

        assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());
        assertNotNull(response.getBody());
    }

}