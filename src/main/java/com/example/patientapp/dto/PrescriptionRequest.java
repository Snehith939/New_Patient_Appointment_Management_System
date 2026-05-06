package com.example.patientapp.dto;

import lombok.Data;

/**
 * Request body for creating or updating a prescription.
 *
 * Used for both POST (create) and PUT (update).
 * On update, only non-null / non-blank fields are applied.
 */
@Data
public class PrescriptionRequest {

    private Long   patientId;
    private Long   doctorId;
    private String medicineName;
    private String dose;
    private String numberOfDays;
    private String instructions;  // optional
}