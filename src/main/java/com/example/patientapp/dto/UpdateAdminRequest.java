package com.example.patientapp.dto;

import lombok.Data;

/**
 * Request body for an admin updating their own personal details.
 *
 * Updatable fields:
 *   name — display name
 *   role — job title within the admin staff (e.g. "SUPER_ADMIN", "MANAGER")
 *
 * NOT updatable here:
 *   email    — login identifier; changing it breaks authentication
 *   password — handled by a dedicated "change password" flow
 */
@Data
public class UpdateAdminRequest {

    private String name;
    private String role;  // job title, e.g. "SUPER_ADMIN"
}