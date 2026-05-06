package com.example.patientapp.model;

/**
 * Represents the lifecycle state of an appointment.
 * Stored as a STRING in the database (not a number) so it's human-readable.
 */
public enum AppointmentStatus {
    BOOKED,
    CANCELED,
    COMPLETED
}