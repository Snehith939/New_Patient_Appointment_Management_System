package com.example.patientapp.controller;

import com.example.patientapp.dto.BlockSlotRequest;
import com.example.patientapp.dto.DoctorScheduleResponse;
import com.example.patientapp.model.BlockedSlot;
import com.example.patientapp.model.Doctor;
import com.example.patientapp.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Doctor self-service operations.
 * Register / login -> AuthController (/api/auth/...)
 *
 * Full endpoint map:
 *   GET    /api/doctors                        list all doctors
 *   POST   /api/doctors                        add a doctor (admin use)
 *   PUT    /api/doctors/{id}/availability      update working hours
 *   GET    /api/doctors/{id}/schedule/today    today's appointments (ordered by time)
 *   GET    /api/doctors/{id}/schedule          full appointment history
 *   POST   /api/doctors/{id}/blocked-slots     block a specific slot
 *   DELETE /api/doctors/{id}/blocked-slots     unblock a slot
 */
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // — List / Add ————————————————————————————————————————————

    // GET /api/doctors
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    // POST /api/doctors
    @PostMapping
    public ResponseEntity<Doctor> addDoctor(@RequestBody Doctor doctor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.addDoctor(doctor));
    }

    // — Availability ——————————————————————————————————————————

    /**
     * PUT /api/doctors/{id}/availability
     * Body: { "startTime": "09:00", "endTime": "17:00" }
     *
     * Updates the doctor's working window. All 15-minute slots within this
     * window become bookable (unless individually blocked).
     */
    @PutMapping("/{id}/availability")
    public ResponseEntity<Doctor> updateDoctorAvailability(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Doctor updated = doctorService.updateDoctorAvailability(
                id, body.get("startTime"), body.get("endTime"));
        return ResponseEntity.ok(updated);
    }

    // — Schedule ——————————————————————————————————————————————

    /**
     * GET /api/doctors/{id}/schedule/today
     *
     * Returns all appointments for today only, ordered earliest-to-latest.
     * Each entry shows: patientName, patientPhone, timeSlot, status.
     *
     * Sample response:
     * [
     *   { "appointmentId":5, "patientName":"Alice", "patientPhone":"9999",
     *     "timeSlot":"09:00", "status":"BOOKED" },
     *   { "appointmentId":7, "patientName":"Bob",   "patientPhone":"8888",
     *     "timeSlot":"10:30", "status":"BOOKED" }
     * ]
     */
    @GetMapping("/{id}/schedule/today")
    public ResponseEntity<List<DoctorScheduleResponse>> getTodaySchedule(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getTodaySchedule(id));
    }

    /**
     * GET /api/doctors/{id}/schedule
     * Returns ALL appointments across all dates.
     */
    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<DoctorScheduleResponse>> getFullSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getFullSchedule(id));
    }

    // — Blocked slots —————————————————————————————————————————

    /**
     * POST /api/doctors/{id}/blocked-slots
     * Body: { "date": "2025-06-15", "timeSlot": "10:00" }
     *
     * Marks the slot as busy. Patients querying available slots on that date
     * will NOT see this slot in the response.
     */
    @PostMapping("/{id}/blocked-slots")
    public ResponseEntity<BlockedSlot> blockSlot(
            @PathVariable Long id,
            @RequestBody BlockSlotRequest request) {

        BlockedSlot blocked = doctorService.blockSlot(id, request.getDate(), request.getTimeSlot());
        return ResponseEntity.status(HttpStatus.CREATED).body(blocked);
    }

    /**
     * DELETE /api/doctors/{id}/blocked-slots
     * Body: { "date": "2025-06-15", "timeSlot": "10:00" }
     *
     * Removes the block, making the slot bookable again.
     */
    @DeleteMapping("/{id}/blocked-slots")
    public ResponseEntity<String> unblockSlot(
            @PathVariable Long id,
            @RequestBody BlockSlotRequest request) {

        doctorService.unblockSlot(id, request.getDate(), request.getTimeSlot());
        return ResponseEntity.ok("Slot " + request.getTimeSlot() + " on "
                + request.getDate() + " has been unblocked.");
    }
}