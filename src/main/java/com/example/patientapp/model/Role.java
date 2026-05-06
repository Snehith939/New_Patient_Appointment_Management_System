package com.example.patientapp.model;

/**
 * Represents which "type" of user is registering.
 * This is different from Admin.role (which is a free-text admin job title).
 */
public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN
}