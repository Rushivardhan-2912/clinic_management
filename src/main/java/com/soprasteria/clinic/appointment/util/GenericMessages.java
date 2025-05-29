package com.soprasteria.clinic.appointment.util;

public class GenericMessages {
    private GenericMessages() {}

    public static final String RESOURCE_NOT_FOUND="The requested resource was not found";
    public static final String USER_NOT_FOUND = "User with ID '%s' not found";
    public static final String FAILED_REGISTRATION="Failed to register.";
    public static final String ALREADY_EXISTING="%s already existed.";

    // Patient-related
    public static final String PATIENT_NOT_FOUND = "Patient with '%s' not found";
    public static final String UNAUTHORIZED= "You are not authorized.";

    // Doctor-related
    public static final String DOCTOR_NOT_FOUND = "Doctor not found with %s";

    //Availability-related
    public static final String AVAILABILITY_NOT_FOUND = "Availability not found with ID: %s";

    // Appointment-related
    public static final String APPOINTMENT_NOT_FOUND = "Appointment not found with ID: %s";
    public static final String INVALID_TIME_RANGE = "Invalid time range.";
    public static final String TIME_SLOT_NOT_AVAILABLE = "The selected time slot is not available.";
    public static final String TIME_SLOT_ALREADY_BOOKED = "Appointment already booked for the same slot.";
}
