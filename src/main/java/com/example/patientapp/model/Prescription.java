package com.example.patientapp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "prescription")
@Data
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long prescriptionId;

    // FK -> patient.patientId
    @ManyToOne
    @JoinColumn(name = "patientId", nullable = false)
    private Patient patient;

    // FK -> doctor.doctorId
    @ManyToOne
    @JoinColumn(name = "doctorId", nullable = false)
    private Doctor doctor;

    private String medicineName;
    private String dose;
    private String numberOfDays;
    private String instructions;  // optional extra notes (e.g. "take after meals")

    private LocalDate prescribedDate;
}