package com.example.patientapp.controller;

import com.example.patientapp.dto.AppointmentResponse;
import com.example.patientapp.dto.BookAppointmentRequest;
import com.example.patientapp.service.AppointmentService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Appointment lifecycle endpoints.
 *
 * Patient-specific views  -> PatientController  (/api/patients/{id}/appointments/...)
 * Doctor-specific views   -> DoctorController   (/api/doctors/{id}/schedule/...)
 *
 * This controller covers:
 *  - Available slot lookup (step 2 of booking)
 *  - Book (step 3 of booking)
 *  - Cancel
 *  - View single / view all  (used by admin or direct lookup)
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // — Available slots ————————————————————————————————————————

    /**
     * GET /api/appointments/available-slots?doctorId=1&date=2025-06-15
     *
     * Returns open 15-minute slots for a doctor on a given date.
     * Excludes: already-booked slots + doctor-blocked slots.
     * Response: ["09:00","09:15","09:30", ...]
     */
    @GetMapping("/available-slots")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ResponseEntity.ok(appointmentService.getAvailableSlots(doctorId, date));
    }

    // — Book ————————————————————————————————————————————————————

    /**
     * POST /api/appointments/book
     * Body: { "patientId":1, "doctorId":2, "appointmentDate":"2025-06-15", "timeSlot":"10:15" }
     *
     * Returns 400 if the slot is already taken, blocked, or outside working hours.
     * Returns 201 with appointment details on success.
     */
    @PostMapping("/book")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.bookAppointment(request));
    }

    // — Cancel ——————————————————————————————————————————————————

    /**
     * PUT /api/appointments/{id}/cancel
     *
     * Sets appointment status to CANCELED.
     * The freed slot immediately becomes available for other patients to book.
     * Can be called by both the patient and the doctor.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    // — View ————————————————————————————————————————————————————

    /**
     * GET /api/appointments/{id}
     * View a single appointment by its ID.
     * Used by both patient and doctor to check a specific booking.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    /**
     * GET /api/appointments
     * View all appointments — primarily for admin oversight.
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> viewAllAppointments() {
        return ResponseEntity.ok(appointmentService.viewAllAppointments());
    }
}