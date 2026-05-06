package com.example.patientapp.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * Request body for updating a patient's personal details.
 *
 * Why not reuse the Patient entity here?
 *   - The Patient entity has fields like email, password, role.
 *   - If we accepted a Patient object, the caller could theoretically
 *     send a new email or password in the body — we'd have to remember
 *     to ignore them in the service. That's error-prone.
 *   - A dedicated DTO makes it impossible to pass the wrong fields:
 *     only these four fields exist, so only these four can be updated.
 *
 * Fields the patient is NOT allowed to change via this endpoint:
 *   email    -> login identifier, changing it breaks authentication
 *   password -> handled by a dedicated "change password" flow
 *   role     -> assigned at registration, cannot self-promote
 */
@Data
public class UpdatePatientRequest {

    private String    name;
    private String    phone;
    private String    address;
    private LocalDate dob;
}