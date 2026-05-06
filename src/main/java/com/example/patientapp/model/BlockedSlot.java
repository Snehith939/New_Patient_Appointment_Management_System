package com.example.patientapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a time slot that a doctor has explicitly blocked — not a
 * patient appointment, just the doctor marking "I'm busy at this time".
 *
 * Why a separate table and not a status on Appointment?
 *   - An Appointment requires a patientId (NOT NULL FK). A blocked slot has
 *     no patient, so we cannot create an Appointment row for it.
 *   - Keeping this separate makes the data model honest: appointments are
 *     between a patient and a doctor; blocked slots are doctor-only.
 *
 * This table is read by AppointmentService.getAvailableSlots() to exclude
 * blocked times from what patients can book.
 */
@Entity
@Table(
        name = "blocked_slot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"doctorId", "blockedDate", "timeSlot"})
)
@Data
public class BlockedSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctorId", nullable = false)
    private Doctor doctor;

    private LocalDate blockedDate;

    private LocalTime timeSlot;
}