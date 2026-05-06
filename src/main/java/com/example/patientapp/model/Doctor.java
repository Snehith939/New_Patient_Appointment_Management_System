package com.example.patientapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Maps to the Doctor table.
 * One necessary addition:
 *   password VARCHAR — required for the login feature.
 * availability is stored as a JSON string:
 *   {"startTime":"09:00","endTime":"17:00"}
 * AppointmentService parses this string at runtime to generate
 * the 15-minute time slots — no separate columns needed.
 */
@Entity
@Table(name = "doctor")
@Data
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    private String name;

    private String specialization;

    private String email;

    private String phone;

    // Stored exactly as the schema defines: JSON column
    // Example value: {"startTime":"09:00","endTime":"17:00"}
    @Column(columnDefinition = "JSON")
    private String availability;

    @JsonIgnore
    private String password;
}