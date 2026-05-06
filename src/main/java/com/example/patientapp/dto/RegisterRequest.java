package com.example.patientapp.dto;

import com.example.patientapp.model.Role;
import lombok.Data;
import java.time.LocalDate;

/**
 * Single request body for registering any role.
 *
 * Required for ALL roles:  name, email, password, phone, role
 * Required for PATIENT:    address, dob
 * Required for DOCTOR:     specialization, startTime, endTime
 *                          (stored in DB as JSON: {"startTime":"09:00","endTime":"17:00"})
 * Required for ADMIN:      adminRole (e.g. "SUPER_ADMIN")
 */
@Data
public class RegisterRequest {

    // — common ————————————————————————————————————————————————
    private String name;
    private String email;
    private String password;
    private String phone;
    private Role   role;          // PATIENT | DOCTOR | ADMIN

    // — PATIENT only ——————————————————————————————————————————
    private String    address;
    private LocalDate dob;

    // — DOCTOR only ———————————————————————————————————————————
    private String specialization;
    private String startTime;     // e.g. "09:00" — stored in availability JSON
    private String endTime;       // e.g. "17:00" — stored in availability JSON

    // — ADMIN only ————————————————————————————————————————————
    private String adminRole;     // free-text admin job title, e.g. "SUPER_ADMIN"
}