package com.example.patientapp.controller;

import com.example.patientapp.dto.LoginRequest;
import com.example.patientapp.dto.RegisterRequest;
import com.example.patientapp.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Single entry point for register and login across all roles.
 *
 * POST /api/auth/register
 *   PATIENT body: { "name":"Alice", "email":"a@x.com", "password":"pw",
 *                   "phone":"9999", "address":"123 St", "dob":"1990-05-20",
 *                   "role":"PATIENT" }
 *
 *   DOCTOR body:  { "name":"Dr Bob", "email":"b@x.com", "password":"pw",
 *                   "phone":"1111", "specialization":"Cardiology",
 *                   "availabilityStart":"09:00", "availabilityEnd":"17:00",
 *                   "role":"DOCTOR" }
 *
 *   ADMIN body:   { "name":"Carol", "email":"c@x.com", "password":"pw",
 *                   "adminRole":"SUPER_ADMIN", "role":"ADMIN" }
 *
 * POST /api/auth/login
 *   Body: { "email":"a@x.com", "password":"pw", "role":"PATIENT" }
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody RegisterRequest request) {
        Object saved = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        String message = authService.login(request);
        return ResponseEntity.ok(message);
    }
}