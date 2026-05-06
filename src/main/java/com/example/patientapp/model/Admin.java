package com.example.patientapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "admin")
@Data
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // Free-text job title within admin staff, e.g. "SUPER_ADMIN", "MANAGER"
    // Different from the Role enum (PATIENT/DOCTOR/ADMIN) which is a system concept
    private String role;

    @JsonIgnore
    private String password;
}