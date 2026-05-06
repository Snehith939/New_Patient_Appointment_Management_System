package com.example.patientapp.dto;

import com.example.patientapp.model.Role;
import lombok.Data;

/**
 * Request body for login.
 * The 'role' tells us which table (patient / doctor / admin) to query.
 */
@Data
public class LoginRequest {

    private String email;
    private String password;
    private Role   role;  // PATIENT | DOCTOR | ADMIN
}