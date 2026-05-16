package com.example.patientapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
//@JsonIgnoreProperties({"password"})
@Table(name = "patient")
@Data
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    private LocalDate dob;

    // @JsonIgnore: password is never included in API JSON responses
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.PATIENT;
}