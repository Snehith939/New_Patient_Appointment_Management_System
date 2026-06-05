package com.example.patientapp.controller;

import com.example.patientapp.dto.AppointmentResponse;
import com.example.patientapp.dto.BookAppointmentRequest;
import com.example.patientapp.service.AppointmentService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // ═══════════════════════════════════════
    //  Helper: Check if logged in
    // ═══════════════════════════════════════
    private Long getLoggedInUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            throw new RuntimeException("Not logged in");
        }
        return userId;
    }

    private String getLoggedInRole(HttpSession session) {
        return (String) session.getAttribute("role");
    }

    // ═══════════════════════════════════════
    //  Available Slots
    // ═══════════════════════════════════════
    @GetMapping("/available-slots")
    public ResponseEntity<?> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            HttpSession session) {

        getLoggedInUserId(session);   // just check if logged in

        return ResponseEntity.ok(
                appointmentService.getAvailableSlots(doctorId, date));
    }

    // ═══════════════════════════════════════
    //  Book — patientId from session
    // ═══════════════════════════════════════
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request,
            HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            String role = getLoggedInRole(session);

            // 🔒 Patient can only book for themselves
            if ("PATIENT".equals(role)) {
                request.setPatientId(userId);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(appointmentService.bookAppointment(request));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════
    //  Cancel — only owner or admin
    // ═══════════════════════════════════════
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            String role = getLoggedInRole(session);

            // 🔒 Patient can only cancel their own
            if ("PATIENT".equals(role)) {
                appointmentService.verifyOwnership(id, userId);
            }

            return ResponseEntity.ok(appointmentService.cancelAppointment(id));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════
    //  My Appointments — only own data
    // ═══════════════════════════════════════
    @GetMapping("/my")
    public ResponseEntity<?> getMyAppointments(HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);

            // 🔒 Returns ONLY this patient's appointments
            return ResponseEntity.ok(
                    appointmentService.getAppointmentsByPatient(userId));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════
    //  View Single — ownership check
    // ═══════════════════════════════════════
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointment(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Long userId = getLoggedInUserId(session);
            String role = getLoggedInRole(session);

            // 🔒 Patient can only view their own
            if ("PATIENT".equals(role)) {
                appointmentService.verifyOwnership(id, userId);
            }

            return ResponseEntity.ok(appointmentService.getAppointmentById(id));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ═══════════════════════════════════════
    //  View All — admin only
    // ═══════════════════════════════════════
    @GetMapping
    public ResponseEntity<?> viewAllAppointments(HttpSession session) {
        try {
            getLoggedInUserId(session);
            return ResponseEntity.ok(appointmentService.viewAllAppointments());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    
}