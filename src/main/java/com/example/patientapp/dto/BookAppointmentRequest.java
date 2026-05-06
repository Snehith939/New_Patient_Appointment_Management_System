package com.example.patientapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request body for booking an appointment.
 *
 * Example JSON:
 * {
 *   "patientId": 1,
 *   "doctorId":  2,
 *   "appointmentDate": "2025-06-15",
 *   "timeSlot": "10:15"
 * }
 *
 * The service will:
 *   1. Verify both IDs exist.
 *   2. Check the timeSlot is one of the doctor's available 15-min slots on that date.
 *   3. Reject if the slot is already booked.
 */
@Data
public class BookAppointmentRequest {

    private Long      patientId;
    private Long      doctorId;
    private LocalDate appointmentDate;
    private LocalTime timeSlot;
}