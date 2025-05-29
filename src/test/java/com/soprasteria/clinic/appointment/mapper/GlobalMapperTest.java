package com.soprasteria.clinic.appointment.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.clinic.appointment.dto.*;
import com.soprasteria.clinic.appointment.entity.*;
import com.soprasteria.clinic.appointment.entity.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class GlobalMapperTest {

    @InjectMocks
    private GlobalMapper globalMapper;

    @Mock
    private ObjectMapper objectMapper; // Mock ObjectMapper

    private Patient patient;
    private PatientDTO patientDTO;
    private Doctor doctor;
    private DoctorDTO doctorDTO;
    private Availability availability;
    private AvailabilityDTO availabilityDTO;
    private Appointment appointment;
    private AppointmentDTO appointmentDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks

        // Prepare entities and DTOs
        patient = new Patient();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setEmail("johndoe@example.com");
        patient.setPhoneNumber("1234567890");
        patient.setAge("30");

        patientDTO = new PatientDTO();
        patientDTO.setId(1L);
        patientDTO.setName("John Doe");
        patientDTO.setEmail("johndoe@example.com");
        patientDTO.setPhoneNumber("1234567890");
        patientDTO.setAge("30");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. Smith");
        doctor.setSpecialization("Cardiology");
        doctor.setUsername("drsmith");

        doctorDTO = new DoctorDTO();
        doctorDTO.setId(1L);
        doctorDTO.setName("Dr. Smith");
        doctorDTO.setSpecialization("Cardiology");
        doctorDTO.setUsername("drsmith");

        availability = new Availability();
        availability.setId(1L);
        availability.setDate(java.time.LocalDate.now());
        availability.setStartTime(java.time.LocalTime.now());
        availability.setEndTime(java.time.LocalTime.now().plusHours(1));
        availability.setStatus(StatusEnum.AVAILABLE);
        availability.setDoctor(doctor);

        availabilityDTO = new AvailabilityDTO();
        availabilityDTO.setId(1L);
        availabilityDTO.setDate(java.time.LocalDate.now());
        availabilityDTO.setStartTime(java.time.LocalTime.now());
        availabilityDTO.setEndTime(java.time.LocalTime.now().plusHours(1));
        availabilityDTO.setStatus(StatusEnum.AVAILABLE);
        availabilityDTO.setDoctor(doctorDTO);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDate(java.time.LocalDate.now());
        appointment.setStartTime(java.time.LocalTime.now());
        appointment.setEndTime(java.time.LocalTime.now().plusHours(1));
        appointment.setStatus(StatusEnum.BOOKED);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        appointmentDTO = new AppointmentDTO();
        appointmentDTO.setId(1L);
        appointmentDTO.setDate(java.time.LocalDate.now());
        appointmentDTO.setStartTime(java.time.LocalTime.now());
        appointmentDTO.setEndTime(java.time.LocalTime.now().plusHours(1));
        appointmentDTO.setStatus(StatusEnum.BOOKED);
        appointmentDTO.setPatient(patientDTO);
        appointmentDTO.setDoctor(doctorDTO);
    }

    @Test
    void testToPatientEntity() {
        // Mock ObjectMapper behavior
        Mockito.when(objectMapper.convertValue(patientDTO, Patient.class)).thenReturn(patient);

        // Act
        Patient result = globalMapper.toPatientEntity(patientDTO);

        // Assert
        assertNotNull(result);
        assertEquals(patient.getId(), result.getId());
        assertEquals(patient.getName(), result.getName());
        assertEquals(patient.getEmail(), result.getEmail());
        assertEquals(patient.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(patient.getAge(), result.getAge());
    }

    @Test
    void testToPatientDTO() {
        // Mock ObjectMapper behavior
        Mockito.when(objectMapper.convertValue(patient, PatientDTO.class)).thenReturn(patientDTO);

        // Act
        PatientDTO result = globalMapper.toPatientDTO(patient);

        // Assert
        assertNotNull(result);
        assertEquals(patientDTO.getId(), result.getId());
        assertEquals(patientDTO.getName(), result.getName());
        assertEquals(patientDTO.getEmail(), result.getEmail());
        assertEquals(patientDTO.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(patientDTO.getAge(), result.getAge());
    }

    @Test
    void testToDoctorEntity() {
        // Mock ObjectMapper behavior
        Mockito.when(objectMapper.convertValue(doctorDTO, Doctor.class)).thenReturn(doctor);

        // Act
        Doctor result = globalMapper.toDoctorEntity(doctorDTO);

        // Assert
        assertNotNull(result);
        assertEquals(doctor.getId(), result.getId());
        assertEquals(doctor.getName(), result.getName());
        assertEquals(doctor.getSpecialization(), result.getSpecialization());
        assertEquals(doctor.getUsername(), result.getUsername());
    }

    @Test
    void testToDoctorDTO() {
        // Mock ObjectMapper behavior
        Mockito.when(objectMapper.convertValue(doctor, DoctorDTO.class)).thenReturn(doctorDTO);

        // Act
        DoctorDTO result = globalMapper.toDoctorDTO(doctor);

        // Assert
        assertNotNull(result);
        assertEquals(doctorDTO.getId(), result.getId());
        assertEquals(doctorDTO.getName(), result.getName());
        assertEquals(doctorDTO.getSpecialization(), result.getSpecialization());
        assertEquals(doctorDTO.getUsername(), result.getUsername());
    }

    @Test
    void testToAvailabilityEntity() {
        // Mock ObjectMapper behavior
        Mockito.when(objectMapper.convertValue(availabilityDTO, Availability.class)).thenReturn(availability);

        // Act
        Availability result = globalMapper.toAvailabilityEntity(availabilityDTO, doctor);

        // Assert
        assertNotNull(result);
        assertEquals(availability.getId(), result.getId());
        assertEquals(availability.getDate(), result.getDate());
        assertEquals(availability.getStartTime(), result.getStartTime());
        assertEquals(availability.getEndTime(), result.getEndTime());
        assertEquals(availability.getStatus(), result.getStatus());
        assertEquals(doctor, result.getDoctor());
    }

    @Test
    void testToAppointmentEntity() {
        // Mock ObjectMapper behavior
        Mockito.when(objectMapper.convertValue(appointmentDTO, Appointment.class)).thenReturn(appointment);

        // Act
        Appointment result = globalMapper.toAppointmentEntity(appointmentDTO, doctor, patient);

        // Assert
        assertNotNull(result);
        assertEquals(appointment.getId(), result.getId());
        assertEquals(appointment.getDate(), result.getDate());
        assertEquals(appointment.getStartTime(), result.getStartTime());
        assertEquals(appointment.getEndTime(), result.getEndTime());
        assertEquals(appointment.getStatus(), result.getStatus());
        assertEquals(patient, result.getPatient());
        assertEquals(doctor, result.getDoctor());
    }

    @Test
    void testToAppointmentDTO() {
        // Mock ObjectMapper behavior for the appointmentDTO conversion
        Mockito.when(objectMapper.convertValue(appointment, AppointmentDTO.class)).thenReturn(appointmentDTO);
        Mockito.when(objectMapper.convertValue(doctor, DoctorDTO.class)).thenReturn(doctorDTO); // Mock doctorDTO conversion
        Mockito.when(objectMapper.convertValue(patient, PatientDTO.class)).thenReturn(patientDTO); // Mock patientDTO conversion

        // Act
        AppointmentDTO result = globalMapper.toAppointmentDTO(appointment);

        // Assert
        assertNotNull(result); // Ensure result is not null
        assertEquals(appointmentDTO.getId(), result.getId());
        assertEquals(appointmentDTO.getDate(), result.getDate());
        assertEquals(appointmentDTO.getStartTime(), result.getStartTime());
        assertEquals(appointmentDTO.getEndTime(), result.getEndTime());
        assertEquals(appointmentDTO.getStatus(), result.getStatus());

        // Assert that patientDTO is correctly set and not null
        assertNotNull(result.getPatient()); // Ensure patient is not null
        assertEquals(patientDTO.getId(), result.getPatient().getId()); // Ensure patient ID matches
        assertEquals(patientDTO.getName(), result.getPatient().getName()); // Ensure patient name matches
        assertEquals(patientDTO.getEmail(), result.getPatient().getEmail()); // Ensure patient email matches

        // Assert that doctorDTO is correctly set and not null
        assertNotNull(result.getDoctor()); // Ensure doctor is not null
        assertEquals(doctorDTO.getId(), result.getDoctor().getId()); // Ensure doctor ID matches
        assertEquals(doctorDTO.getName(), result.getDoctor().getName()); // Ensure doctor name matches
    }

    @Test
    void testToAvailabilityDTO() {
        // Mock ObjectMapper behavior for the doctor DTO conversion
        Mockito.when(objectMapper.convertValue(availability, AvailabilityDTO.class)).thenReturn(availabilityDTO);
        Mockito.when(objectMapper.convertValue(doctor, DoctorDTO.class)).thenReturn(doctorDTO); // Mock doctorDTO conversion

        // Act
        AvailabilityDTO result = globalMapper.toAvailabilityDTO(availability);

        // Assert
        assertNotNull(result); // Ensure result is not null
        assertEquals(availabilityDTO.getId(), result.getId());
        assertEquals(availabilityDTO.getDate(), result.getDate());
        assertEquals(availabilityDTO.getStartTime(), result.getStartTime());
        assertEquals(availabilityDTO.getEndTime(), result.getEndTime());
        assertEquals(availabilityDTO.getStatus(), result.getStatus());

        // Assert that doctorDTO is correctly set and not null
        assertNotNull(result.getDoctor()); // Ensure doctor is not null
        assertEquals(doctorDTO.getId(), result.getDoctor().getId()); // Ensure doctor ID matches
        assertEquals(doctorDTO.getName(), result.getDoctor().getName()); // Ensure doctor name matches
        assertEquals(doctorDTO.getSpecialization(), result.getDoctor().getSpecialization()); // Ensure doctor specialization matches
    }

}
