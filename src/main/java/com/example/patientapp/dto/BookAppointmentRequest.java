package com.example.patientapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
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

//	@NotNull(message = "Patient ID is required")
    private Long      patientId;
    
    @NotNull(message = "Doctor ID is required")
    private Long      doctorId;
    
 
    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(message = "Cannot book appointments in the past")
    private LocalDate appointmentDate;
 
    
    @NotNull(message = "Time slot is required")
    private LocalTime timeSlot;
}