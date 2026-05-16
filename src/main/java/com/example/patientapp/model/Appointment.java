package com.example.patientapp.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JPA Entity mapped to the 'appointment' table.
 *
 * Key concepts demonstrated here:
 *   @ManyToOne  -> Many appointments can belong to one Patient/Doctor (FK relationship)
 *   @JoinColumn -> Specifies the foreign key column name in this table
 *   @Enumerated -> Stores enum as a String ("BOOKED") not an integer (0)
 */
@Entity
@Table(name = "appointment")
@Data
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appointmentId;

    // FK -> patient.patientId
    @ManyToOne
    @JoinColumn(name = "patientId", nullable = false)
    @JsonIgnoreProperties({"appointments"})
    private Patient patient;

    // FK -> doctor.doctorId
    @ManyToOne
    @JoinColumn(name = "doctorId", nullable = false)
    @JsonIgnoreProperties({"appointments"})
    private Doctor doctor;

    private LocalDate appointmentDate;

    private LocalTime timeSlot;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
}