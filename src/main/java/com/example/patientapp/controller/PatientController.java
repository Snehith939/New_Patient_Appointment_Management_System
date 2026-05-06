package com.example.patientapp.controller;

import com.example.patientapp.dto.AppointmentResponse;
import com.example.patientapp.dto.UpdatePatientRequest;
import com.example.patientapp.model.Patient;
import com.example.patientapp.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Patient self-service operations.
 * Register / login -> AuthController  (/api/auth/...)
 * Book / cancel   -> AppointmentController (/api/appointments/...)
 */
@RestController 
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    // — Update personal details ———————————————————————————————

    /**
     * PUT /api/patients/{id}
     *
     * Body (only these four fields — email/password/role are intentionally excluded):
     * {
     *   "name":    "Alice Smith",
     *   "phone":   "9876543210",
     *   "address": "42 Elm Street",
     *   "dob":     "1990-05-20"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatientProfile(
            @PathVariable Long id,
            @RequestBody UpdatePatientRequest request) {

        Patient updated = patientService.updatePatientProfile(id, request);
        return ResponseEntity.ok(updated);
    }

    // — Appointment history ———————————————————————————————————

    /**
     * GET /api/patients/{id}/appointments/history
     *
     * Returns all past appointments (date < today), ordered newest first.
     * Each item shows: doctorName, doctorSpecialization, date, timeSlot, status.
     *
     * Sample response:
     * [
     *   {
     *     "appointmentId": 3,
     *     "doctorId": 2,
     *     "doctorName": "Dr. Smith",
     *     "doctorSpecialization": "Cardiology",
     *     "doctorPhone": "1234567890",
     *     "appointmentDate": "2025-05-01",
     *     "timeSlot": "10:15:00",
     *     "status": "COMPLETED"
     *   }, ...
     * ]
     */
    @GetMapping("/{id}/appointments/history")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentHistory(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getAppointmentHistory(id));
    }

    // — Upcoming appointments —————————————————————————————————

    /**
     * GET /api/patients/{id}/appointments/upcoming
     *
     * Returns all future BOOKED appointments, ordered soonest first.
     * Cancelled appointments do NOT appear here.
     */
    @GetMapping("/{id}/appointments/upcoming")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingAppointments(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getUpcomingAppointments(id));
    }
}