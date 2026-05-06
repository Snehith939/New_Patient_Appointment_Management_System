package com.example.patientapp.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request body for blocking or unblocking a time slot.
 *
 * POST   /api/doctors/{id}/blocked-slots  -> block
 * DELETE /api/doctors/{id}/blocked-slots  -> unblock
 *
 * Example body:
 * {
 *   "date":     "2025-06-15",
 *   "timeSlot": "10:00"
 * }
 */
@Data
public class BlockSlotRequest {

    private LocalDate date;
    private LocalTime timeSlot;
}